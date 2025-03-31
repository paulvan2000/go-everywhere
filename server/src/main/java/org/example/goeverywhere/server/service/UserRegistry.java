// src/main/java/org/example/goeverywhere/server/service/UserRegistry.java
package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.service.routing.OptimizedRouteService;
import org.example.goeverywhere.server.service.routing.RouteService; // Keep RouteService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant; // Import Instant
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class UserRegistry {

    // e.g., 20 meters
    public static final double WAYPOINT_REMOVAL_THRESHOLD_METERS = 20.0;
    public static final double EARTH_RADIUS = 6371; // kilometers
    public static final int METERS_IN_KILOMETER = 1000;
    // Max distance (meters) for a driver to be considered for a batch route start
    private static final double BATCH_START_MAX_DISTANCE_METERS = 15000; // 15km
    // Max ETA (minutes) for an idle driver to reach pickup for an immediate ride
    private static final double IMMEDIATE_PICKUP_MAX_ETA_MINUTES = 15.0;


    @Autowired
    SessionStore sessionStore;

    // Decide which RouteService is primary (e.g., Optimized for merging)
    @Autowired
    @Qualifier("optimizedRouteService") // Using Optimized for merging/generation
            RouteService routeService;

    private final ConcurrentHashMap<String, Driver> drivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Rider> riders = new ConcurrentHashMap<>();

    // --- Registration methods (unchanged) ---
    public void registerRider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if (userType != UserType.RIDER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a RIDER");
        }
        riders.computeIfAbsent(sessionId, k -> new Rider(k, streamObserver)); // Use computeIfAbsent
    }

    public void unregisterRider(String sessionId) {
        // Optional: Check userType before removing if strictness needed
        riders.remove(sessionId);
        System.out.println("Unregistered rider: " + sessionId);
    }

    public void registerDriver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if (userType != UserType.DRIVER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a DRIVER");
        }
        drivers.computeIfAbsent(sessionId, k -> new Driver(k, streamObserver)); // Use computeIfAbsent
    }

    public void unregisterDriver(String sessionId) { // Added for completeness
        drivers.remove(sessionId);
        System.out.println("Unregistered driver: " + sessionId);
    }

    // --- updateUserLocation & checkAndRemoveWaypoint (with immutable update fix) ---
    public void updateUserLocation(String sessionId, LatLng location) {
        try {
            UserType userType = sessionStore.getUserType(sessionId);
            switch (userType) {
                case RIDER -> {
                    Rider rider = riders.get(sessionId);
                    if (rider != null) {
                        rider.location = location;
                    } else {
                        System.out.println("Rider not yet registered with sessionId " + sessionId);
                    }
                }
                case DRIVER -> {
                    Driver driver = drivers.get(sessionId);
                    if (driver != null) {
                        driver.location = location;
                        checkAndRemoveWaypoint(driver); // Check if near waypoint
                    } else {
                        System.out.println("Driver not yet registered with sessionId " + sessionId);
                    }
                }
                default -> System.err.println("Unknown user type in updateUserLocation: " + userType);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating location (session likely invalid): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error updating location: " + e.getMessage());
            // Log stack trace if needed: e.printStackTrace();
        }
    }

    private void checkAndRemoveWaypoint(Driver driver) {
        Route currentRoute = driver.getCurrentFullRoute(); // Get atomic reference
        if (currentRoute == null || currentRoute.getWaypointsList().isEmpty()) {
            return; // No route or empty route
        }

        Waypoint nextWaypoint = currentRoute.getWaypoints(0);
        if (driver.location == null) return; // Cannot check distance without driver location

        double distanceMeters = calculateDistance(driver.location, nextWaypoint.getLocation());

        if (distanceMeters <= WAYPOINT_REMOVAL_THRESHOLD_METERS) {
            // Build a new route minus the first waypoint
            Route.Builder updatedRouteBuilder = currentRoute.toBuilder();
            updatedRouteBuilder.removeWaypoints(0);

            // Atomically update the driver's route reference
            driver.setCurrentFullRoute(updatedRouteBuilder.build()); // <<< Use setter

            System.out.println("Removed waypoint for driver " + driver.getSessionId() + ". New count: " + driver.getCurrentFullRoute().getWaypointsCount());
            // TODO: Optionally, notify driver/riders about waypoint removal if needed
        }
    }

    // --- Getters (unchanged) ---
    public Optional<Driver> getDriverMaybe(String sessionId) {
        return Optional.ofNullable(drivers.get(sessionId));
    }
    public Optional<Rider> getRiderMaybe(String sessionId) {
        return Optional.ofNullable(riders.get(sessionId));
    }
    public Collection<Driver> getAvailableDrivers() { // Helper needed for batch depot selection
        return drivers.values().stream()
                .filter(Driver::isAvailableNow) // Use availability check
                .collect(Collectors.toList());
    }


    // --- Refined findAvailableDriverAndNewRoute (Immediate Rides) ---
    public Optional<Pair<Driver, Route>> findAvailableDriverAndNewRoute(Route riderRouteAtoB, String rideId) {
        requireNonNull(riderRouteAtoB, "riderRouteAtoB cannot be null");
        if (riderRouteAtoB.getWaypointsCount() < 2) {
            System.err.println("Invalid riderRouteAtoB (needs at least 2 waypoints)");
            return Optional.empty();
        }

        Waypoint originWaypoint = riderRouteAtoB.getWaypoints(0);
        LatLng riderOrigin = originWaypoint.getLocation();
        String riderSessionId = originWaypoint.getWaypointMetadataList().stream()
                .filter(m -> m.getWaypointType() == WaypointType.ORIGIN)
                .map(WaypointMetadata::getRiderId)
                .findFirst().orElse(null); // Get rider ID from metadata if possible

        if (riderSessionId == null) {
            System.err.println("Could not determine riderSessionId from riderRouteAtoB metadata");
            // Fallback or error - maybe pass riderSessionId explicitly? For now, continue without it for matching.
        }


        // --- Strategy 1: Find best idle driver ---
        Optional<Pair<Driver, Route>> bestIdleOption = findBestIdleDriver(riderOrigin, rideId);

        // --- Strategy 2: Find best merge with active driver ---
        Optional<Pair<Driver, Route>> bestMergeOption = findBestMergeDriver(riderRouteAtoB, rideId);

        // --- Combine Strategies: Prefer idle if good ETA, otherwise consider merge ---
        if (bestIdleOption.isPresent()) {
            // Check if idle ETA is acceptable (using route duration as proxy)
            double idleEtaMinutes = bestIdleOption.get().getSecond().getTotalDurationMin();
            if (idleEtaMinutes <= IMMEDIATE_PICKUP_MAX_ETA_MINUTES) {
                System.out.println("Assigning immediate ride " + rideId + " to idle driver " + bestIdleOption.get().getFirst().getSessionId() + " (ETA: " + idleEtaMinutes + " min)");
                return bestIdleOption;
            }
            // Idle driver too far, consider merge
            if (bestMergeOption.isPresent()) {
                System.out.println("Idle driver too far for ride " + rideId + ". Assigning to merging driver " + bestMergeOption.get().getFirst().getSessionId());
                return bestMergeOption;
            } else {
                System.out.println("Idle driver too far for ride " + rideId + ", and no merge options. Assigning to idle driver anyway.");
                return bestIdleOption; // Fallback to the far idle driver if no merge
            }
        } else if (bestMergeOption.isPresent()) {
            System.out.println("No suitable idle driver found for ride " + rideId + ". Assigning to merging driver " + bestMergeOption.get().getFirst().getSessionId());
            return bestMergeOption;
        } else {
            System.out.println("No idle or merging driver found for immediate ride " + rideId);
            return Optional.empty(); // No driver found
        }
    }

    // Helper for finding best idle driver
    private Optional<Pair<Driver, Route>> findBestIdleDriver(LatLng riderOrigin, String rideId) {
        return drivers.values().stream()
                .filter(Driver::isAvailableNow) // Check if truly available
                .filter(d -> d.location != null)
                .filter(d -> !d.rejectedRides.contains(rideId))
                .map(driver -> {
                    // Calculate route from Driver location to Rider Origin
                    try {
                        Route routeToPickup = routeService.generateRoute(driver.location, riderOrigin, null); // No rider ID needed for this segment
                        return Pair.of(driver, routeToPickup);
                    } catch (Exception e) {
                        System.err.println("Error generating route to pickup for idle driver " + driver.getSessionId() + ": " + e.getMessage());
                        return Pair.of(driver, (Route) null); // Mark as failed
                    }
                })
                .filter(pair -> pair.getSecond() != null && pair.getSecond().getTotalDurationMin() > 0) // Filter out failed route generations or zero duration
                .min(Comparator.comparingDouble(pair -> pair.getSecond().getTotalDurationMin())) // Find driver with minimum ETA
                .map(bestPair -> {
                    // Now construct the *full* route Driver -> A -> B for the assignment
                    Driver driver = bestPair.getFirst();
                    Route routeToA = bestPair.getSecond();
                    // Need the rider's destination B
                    // This requires getting Rider B location, ideally from riderRouteAtoB passed earlier
                    // Assuming riderRouteAtoB was Rider A -> Rider B
                    LatLng riderDestination = routeToA.getWaypoints(routeToA.getWaypointsCount()-1).getLocation();
                    String riderSessionId = routeToA.getWaypoints(0).getWaypointMetadataList().stream()
                            .filter(m->m.getWaypointType()==WaypointType.ORIGIN)
                            .map(WaypointMetadata::getRiderId).findFirst().orElse(null);

                    // Call OR-Tools to generate the combined D->A->B route properly
                    try {
                        Route combinedRoute = ((OptimizedRouteService) routeService).generateMergedRoute(
                                driver.location,
                                List.of(Pair.of(riderSessionId, riderOrigin)),
                                List.of(Pair.of(riderSessionId, riderDestination))
                        );
                        return Pair.of(driver, combinedRoute);
                    } catch (Exception e) {
                        System.err.println("Error generating combined D->A->B route for idle driver " + driver.getSessionId() + ": " + e.getMessage());
                        return null; // Failed to create final route
                    }
                })
                .filter(Objects::nonNull); // Filter out failures from combined route generation
    }

    // Helper for finding best merge driver
    private Optional<Pair<Driver, Route>> findBestMergeDriver(Route riderRouteAtoB, String rideId) {
        return drivers.values().stream()
                .filter(driver -> !driver.isAvailableNow() && driver.getCurrentFullRoute() != null) // Active drivers with routes
                .filter(d -> !d.rejectedRides.contains(rideId))
                .map(driver -> {
                    Optional<Route> mergedRouteOpt = routeService.tryMergeRoutes(driver.getCurrentFullRoute(), riderRouteAtoB);
                    return mergedRouteOpt.map(mergedRoute -> Pair.of(driver, mergedRoute)); // Pair<Driver, MergedRoute>
                })
                .filter(Optional::isPresent) // Keep only successful merges
                .map(Optional::get)
                // TODO: Add more sophisticated cost comparison here if needed
                // For now, just take the first successful merge found (can be improved by sorting by detour/cost)
                .findFirst();
    }


    // --- Refined findAvailableDriverForBatchRoute ---
    public Optional<Driver> findAvailableDriverForBatchRoute(Route batchRoute) {
        if (batchRoute == null || batchRoute.getWaypointsCount() == 0) {
            return Optional.empty();
        }

        LatLng routeStartLocation = batchRoute.getWaypoints(0).getLocation();
        // TODO: Estimate required start time more accurately if needed

        Instant now = Instant.now(); // Consider current time

        return drivers.values().stream()
                .filter(Driver::isAvailableNow) // Check basic flag + potentially time window
                .filter(d -> d.location != null)
                .filter(d -> calculateDistance(d.location, routeStartLocation) <= BATCH_START_MAX_DISTANCE_METERS)
                // TODO: Add future checks: capacity, working hours, zone restrictions
                // .filter(d -> d.getVehicleCapacity() >= calculateMaxConcurrentPassengers(batchRoute))
                // .filter(d -> d.isWorkingInWindow(estimatedStartTime, estimatedEndTime))
                .min(Comparator.comparingDouble(d -> calculateDistance(d.location, routeStartLocation))) // Closest suitable driver
                .map(driver -> {
                    System.out.println("Selected driver " + driver.getSessionId() + " for batch route starting near " + routeStartLocation);
                    // Mark driver as potentially busy for this batch (optional, depends on workflow)
                    // driver.markAsTentativelyAssigned(batchRoute);
                    return driver;
                });
    }


    // --- calculateDistance (already provided) ---
    public static double calculateDistance(LatLng latLng1, LatLng latLng2) {
        // ... (Haversine implementation) ...
        if (latLng1 == null || latLng2 == null) return Double.MAX_VALUE;
        double dLat = Math.toRadians(latLng2.getLatitude() - latLng1.getLatitude());
        double dLon = Math.toRadians(latLng2.getLongitude() - latLng1.getLongitude());
        double lat1Rad = Math.toRadians(latLng1.getLatitude());
        double lat2Rad = Math.toRadians(latLng2.getLatitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c * METERS_IN_KILOMETER; // Distance in meters
    }

    // --- Inner classes Driver and Rider (Add availability check to Driver) ---
    private abstract static class AbstractUser<T> {
        // ... (fields sessionId, streamObserver, location) ...
        final String sessionId; // Make final
        final StreamObserver<T> streamObserver; // Make final
        volatile LatLng location;

        public AbstractUser(String sessionId, StreamObserver<T> streamObserver) {
            this.sessionId = requireNonNull(sessionId, "sessionId cannot be null");
            this.streamObserver = requireNonNull(streamObserver, "streamObserver cannot be null");
        }
        public String getSessionId() { return sessionId; }
        public StreamObserver<T> getStreamObserver() { return streamObserver; }
        public LatLng getLocation() { return location; } // Getter for location
    }

    public static class Driver extends AbstractUser<DriverEvent> {
        private final Set<String> rejectedRides = ConcurrentHashMap.newKeySet();
        private final Set<String> currentRideIds = ConcurrentHashMap.newKeySet(); // Holds rider IDs currently assigned/onboard
        private volatile Route currentFullRoute;
        private volatile boolean isAvailable = true; // Basic availability flag
        private volatile Instant availableSince = Instant.now(); // Track when driver became available

        public Driver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
            super(sessionId, streamObserver);
        }

        public void addRejectedRide(String rideId) {
            if (rideId != null) rejectedRides.add(rideId);
        }
        public Set<String> getRejectedRideIds() { return rejectedRides; } // Getter needed for filtering


        public Set<String> getCurrentRideIds() {
            return Collections.unmodifiableSet(currentRideIds); // Return unmodifiable view
        }

        // Method to add a rider (used in EventProcessor.driverAccepted)
        public synchronized void addRider(String riderId) {
            currentRideIds.add(riderId);
            isAvailable = false; // Driver is busy when they have riders
        }

        // Method to remove a rider (used in EventProcessor.rideCompleted)
        public synchronized void removeRider(String riderId) {
            currentRideIds.remove(riderId);
            if (currentRideIds.isEmpty()) {
                setCurrentFullRoute(null); // Clear route when last rider dropped off
                setAvailable(true); // Become available again
            }
        }

        public synchronized void setCurrentFullRoute(Route route) {
            this.currentFullRoute = route;
            // If setting a route, driver is generally not available for new immediate requests
            // unless merging is intended. Batch assignment might override this later.
            if (route != null && !route.getWaypointsList().isEmpty()) {
                // isAvailable = false; // Consider if setting a route always means busy
            } else if (currentRideIds.isEmpty()) {
                // If route is cleared and no riders, become available
                setAvailable(true);
            }
        }

        public Route getCurrentFullRoute() { // Synchronize access if needed, but volatile might suffice
            return currentFullRoute;
        }

        public synchronized void setAvailable(boolean available) {
            if (available && !this.isAvailable) {
                availableSince = Instant.now(); // Record time when became available
            }
            this.isAvailable = available;
            System.out.println("Driver " + getSessionId() + " availability set to: " + available);
        }

        public synchronized boolean isAvailableNow() {
            // Basic check, could add time window checks later
            return isAvailable;
        }

        public synchronized Instant getAvailableSince() {
            return availableSince;
        }
    }

    public static class Rider extends AbstractUser<RiderEvent> {
        volatile Route currentRoute; // The segment relevant to the rider
        volatile boolean isPickedUp = false;
        volatile LatLng origin;
        volatile LatLng destination;
        // Store original request for re-queueing
        RideRequest originalRequest;

        public Rider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
            super(sessionId, streamObserver);
        }

        // Getters and Setters for origin, destination, isPickedUp, currentRoute
        public LatLng getOrigin() { return origin; }
        public void setOrigin(LatLng origin) { this.origin = origin; }
        public LatLng getDestination() { return destination; }
        public void setDestination(LatLng destination) { this.destination = destination; }
        public boolean isPickedUp() { return isPickedUp; } // Renamed getter
        public void setPickedUp(boolean pickedUp) { isPickedUp = pickedUp; }
        public Route getCurrentRoute() { return currentRoute; }
        public void setCurrentRoute(Route currentRoute) { this.currentRoute = currentRoute; }
        public RideRequest getOriginalRequest() { return originalRequest; } // Getter for original request
        public void setOriginalRequest(RideRequest originalRequest) { this.originalRequest = originalRequest; } // Setter

    }

} // End of UserRegistry class
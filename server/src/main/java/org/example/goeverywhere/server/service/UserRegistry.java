package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.service.routing.GoogleMapsRouteService;
import org.example.goeverywhere.server.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class UserRegistry {

    // e.g. 20 meters
    public static final double WAYPOINT_REMOVAL_THRESHOLD_METERS = 20.0;
    @Autowired
    SessionStore sessionStore;
    @Autowired
    @Qualifier("mockRouteService")
    RouteService routeService;

    // Radius of Earth in km
    public static final double EARTH_RADIUS = 6371;
    public static final int METERS_IN_KILOMETER = 1000;
    private ConcurrentHashMap<String, Driver> drivers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Rider> riders  = new ConcurrentHashMap<>();

    public void registerRider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if(userType != UserType.RIDER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a RIDER");
        }
        riders.put(sessionId, new Rider(sessionId, streamObserver));
    }

    public void unregisterRider(String sessionId) {
        UserType userType = sessionStore.getUserType(sessionId);
        if(userType != UserType.RIDER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a RIDER");
        }
        riders.remove(sessionId);
    }

    public void registerDriver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if(userType != UserType.DRIVER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a DRIVER");
        }

        drivers.put(sessionId, new Driver(sessionId, streamObserver));
    }

    public void updateUserLocation(String sessionId, LatLng location) {
        UserType userType = sessionStore.getUserType(sessionId);
        switch (userType) {
            case RIDER -> {
                Rider rider = riders.get(sessionId);
                if(rider == null) {
                    throw new IllegalArgumentException("Unknown sessionId " + sessionId);
                }
                rider.location = location;
            }
            case DRIVER -> {
                Driver driver = drivers.get(sessionId);
                if(driver == null) {
                    throw new IllegalArgumentException("Unknown sessionId " + sessionId);
                }
                driver.location = location;
                // Attempt to remove the waypoint if the driver is close enough
                checkAndRemoveWaypoint(driver);
            }
        }

    }

    /**
     * Checks if the driver is close enough to the next waypoint in the route.
     * If within a threshold, remove that waypoint from the route.
     */
    private void checkAndRemoveWaypoint(Driver driver) {
        if (driver.currentFullRoute == null) {
            return; // No route to process
        }

        var route = driver.currentFullRoute;

        // If no waypoints left, nothing to do
        if (route.getWaypointsList().isEmpty()) {
            return;
        }

        // Let's look at the first waypoint
        var nextWaypoint = route.getWaypoints(0);
        double distanceMeters = RouteService.calculateDistance(driver.location, nextWaypoint.getLocation());

        // If driver is within threshold, remove it from the route
        if (distanceMeters <= WAYPOINT_REMOVAL_THRESHOLD_METERS) {
            // Build a new route minus the first waypoint
            Route.Builder updatedRoute = route.toBuilder();
            updatedRoute.removeWaypoints(0); // remove the next waypoint
            // Update the driver's route
            driver.setCurrentFullRoute(updatedRoute.build());

            System.out.println("Removed first waypoint. Next waypoint count: " + driver.currentFullRoute.getWaypointsCount());
        }
    }

    public Optional<Driver> getDriverMaybe(String sessionId) {
        return Optional.ofNullable(drivers.get(sessionId));
    }

    public Optional<Rider> getRiderMaybe(String sessionId) {
        return Optional.ofNullable(riders.get(sessionId));
    }



    /**
     * This method looks for an available driver. All the registered drivers are sorted according to their distance
     * from the rider. If the driver is idle, it takes riders route for consideration. If the driver is en route,
     * the method attempts to merge the existing route
     *
     * The compatibility criteria is defined in {@link GoogleMapsRouteService}.
     * @param route a proposed rider's route
     * @param rideId rider id
     * @return an empty optional if there is no driver meeting criteria, an optional with a pair of values if there is such a driver.
     * A pair of values consists of a driver and his new route.
     *
     */
    public Optional<Pair<Driver, Route>> findAvailableDriverAndNewRoute(Route route, String rideId) {
        Waypoint origin = route.getWaypointsList().get(0);
        List<Driver> sorterDrivers = drivers.entrySet().parallelStream()
                .filter(v -> {
                    Driver driver = v.getValue();
                    return driver.location != null && !driver.rejectedRides.contains(rideId);
                })
                .sorted((kv1, kv2) ->
                        (int) (RouteService.calculateDistance(kv1.getValue().location, origin.getLocation()) - RouteService.calculateDistance(kv2.getValue().location, origin.getLocation()))
                )
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        for (Driver driver : sorterDrivers) {
            Route currentRoute = driver.currentFullRoute;
            if(currentRoute == null) {
                // if the driver is idle then there is no need to merge, we take rider's route
                return Optional.of(Pair.of(driver, route));
            }
            // checking if the driver's and rider's routes are mergeable
            Optional<Route> newRouteOpt = routeService.tryMergeRoutes(currentRoute, route);
            if(newRouteOpt.isPresent()) {
                return Optional.of(Pair.of(driver, newRouteOpt.get()));
            }
        }

        return Optional.empty();
    }


    private abstract static class AbstractUser<T> {

        private final String sessionId;
        private final StreamObserver<T> streamObserver;

        volatile LatLng location;

        public AbstractUser(String sessionId, StreamObserver<T> streamObserver) {
            this.sessionId = sessionId;
            this.streamObserver = streamObserver;
        }

        public String getSessionId() {
            return sessionId;
        }

        public StreamObserver<T> getStreamObserver() {
            return streamObserver;
        }

        public LatLng getLocation() {
            return location;
        }
    }

    public static class Driver extends AbstractUser<DriverEvent> {
        private final Set<String> rejectedRides = ConcurrentHashMap.newKeySet();
        private final Set<String> currentRideIds = ConcurrentHashMap.newKeySet();

        // FIXME: Current route must be updated every time the driver passes a way point from the route
        private volatile Route currentFullRoute;

        public Driver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
            super(sessionId, streamObserver);
        }

        public void addRejectedRide(String rideId) {
            rejectedRides.add(rideId);
        }

        public Set<String> getCurrentRideIds() {
            return currentRideIds;
        }

        public void setCurrentFullRoute(Route route) {
            currentFullRoute = route;
        }

        public Route getCurrentFullRoute() {
            return currentFullRoute;
        }
    }

    public static class Rider extends AbstractUser<RiderEvent> {
        volatile Route currentRoute;
        volatile boolean isPickedUp = false;
        volatile LatLng origin;
        volatile LatLng destination;


        public LatLng getOrigin() {
            return origin;
        }

        public void setOrigin(LatLng origin) {
            this.origin = origin;
        }

        public LatLng getDestination() {
            return destination;
        }

        public void setDestination(LatLng destination) {
            this.destination = destination;
        }

        public Route getCurrentRoute() {
            return currentRoute;
        }

        public void setCurrentRoute(Route currentRoute) {
            this.currentRoute = currentRoute;
        }

        public void setPickedUp(boolean pickedUp) {
            isPickedUp = pickedUp;
        }

        public Rider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
            super(sessionId, streamObserver);
        }
    }

}

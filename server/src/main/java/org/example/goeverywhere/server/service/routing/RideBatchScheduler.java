// src/main/java/org/example/goeverywhere/server/service/RideBatchScheduler.java
package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineConfig;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.routing.OptimizedRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair; // Use Spring's Pair
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection; // Import Collection
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional; // Import Optional
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class RideBatchScheduler {

    private final List<RideRequest> pendingRides = new CopyOnWriteArrayList<>();

    @Autowired
    private OptimizedRouteService optimizedRouteService; // Use Optimized service directly

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private UserRegistry userRegistry;

    public void addRide(RideRequest request) {
        pendingRides.add(request);
    }

    @Scheduled(fixedRateString = "${goeverywhere.batch.rate:900000}", initialDelayString = "${goeverywhere.batch.delay:60000}") // Configurable rate (default 15min), 1 min initial delay
    public void processBatch() {
        List<RideRequest> ridesToProcess = selectRidesForProcessing();
        if (ridesToProcess.isEmpty()) {
            return;
        }
        System.out.println("Processing batch of " + ridesToProcess.size() + " scheduled rides.");

        // Aggregate pickups and dropoffs, mapping rider ID to location
        List<Pair<String, LatLng>> pickups = new ArrayList<>();
        List<Pair<String, LatLng>> dropoffs = new ArrayList<>();
        if (!extractLocations(ridesToProcess, pickups, dropoffs)) {
            System.err.println("Batch processing aborted due to geocoding errors.");
            // TODO: Handle geocoding failures - maybe retry individual rides?
            pendingRides.removeAll(ridesToProcess); // Remove failed batch
            return;
        }

        // --- Enhanced Depot Selection ---
        Optional<LatLng> selectedDepotOpt = selectDepotForBatch(pickups);
        if (selectedDepotOpt.isEmpty()) {
            System.err.println("Could not determine a suitable depot (no drivers nearby?). Aborting batch.");
            // Re-queue or cancel? For now, just remove them.
            pendingRides.removeAll(ridesToProcess);
            return;
        }
        LatLng depot = selectedDepotOpt.get();
        System.out.println("Selected depot for batch route: " + depot);


        // --- Generate Merged Route using Optimized Service ---
        Route mergedRoute;
        try {
            mergedRoute = optimizedRouteService.generateMergedRoute(depot, pickups, dropoffs);
            System.out.println("Generated merged route with " + mergedRoute.getWaypointsCount() + " waypoints.");
        } catch (Exception e) {
            System.err.println("Failed to generate merged route for batch: " + e.getMessage());
            // Re-queue or cancel? For now, just remove them.
            pendingRides.removeAll(ridesToProcess);
            return;
        }

        // Remove processed rides *after* successful route generation
        pendingRides.removeAll(ridesToProcess);


        // --- Trigger State Machines for each ride in the batch ---
        triggerStateMachines(ridesToProcess, mergedRoute);
    }

    private List<RideRequest> selectRidesForProcessing() {
        if (pendingRides.isEmpty()) {
            return Collections.emptyList();
        }
        long now = Instant.now().getEpochSecond();
        // Configurable planning window (e.g., from application.properties)
        long planningWindowSeconds = 7200; // Default 2 hours

        return pendingRides.stream()
                .filter(ride -> ride.getScheduledPickupTime() > 0 && ride.getScheduledPickupTime() <= now + planningWindowSeconds)
                .collect(Collectors.toList());
    }

    private boolean extractLocations(List<RideRequest> rides, List<Pair<String, LatLng>> pickups, List<Pair<String, LatLng>> dropoffs) {
        boolean allDecoded = true;
        for (RideRequest ride : rides) {
            try {
                LatLng origin = geocodingService.decodeAddress(ride.getOrigin());
                LatLng destination = geocodingService.decodeAddress(ride.getDestination());
                pickups.add(Pair.of(ride.getSessionId(), origin));
                dropoffs.add(Pair.of(ride.getSessionId(), destination));
            } catch (Exception e) {
                System.err.println("Error decoding addresses for ride request (rider: " + ride.getSessionId() + "): " + e.getMessage());
                allDecoded = false;
                // Optionally remove this specific ride from ridesToProcess list here
            }
        }
        return allDecoded;
    }

    private Optional<LatLng> selectDepotForBatch(List<Pair<String, LatLng>> pickups) {
        if (pickups.isEmpty()) {
            return Optional.empty();
        }

        // Calculate rough center of pickups (optional, could just use first pickup)
        LatLng batchCenter = pickups.get(0).getSecond(); // Simplification: use first pickup as center

        // Get available drivers
        Collection<UserRegistry.Driver> availableDrivers = userRegistry.getAvailableDrivers();

        // Find the available driver closest to the batch center
        Optional<UserRegistry.Driver> closestDriverOpt = availableDrivers.stream()
                .filter(d -> d.getLocation() != null)
                .min(Comparator.comparingDouble(d -> UserRegistry.calculateDistance(d.getLocation(), batchCenter)));

        // Use driver's location if found, otherwise fallback to first pickup
        return closestDriverOpt.map(UserRegistry.Driver::getLocation)
                .or(() -> {
                    System.out.println("No nearby available drivers found for batch depot selection. Using first pickup as fallback depot.");
                    return Optional.of(pickups.get(0).getSecond());
                });
    }


    private void triggerStateMachines(List<RideRequest> rides, Route mergedRoute) {
        for (RideRequest ride : rides) {
            String riderSessionId = ride.getSessionId();
            try {
                // Ensure rider object exists to store original request (may need adjustment if rider isn't registered yet)
                UserRegistry.Rider rider = userRegistry.getRiderMaybe(riderSessionId).orElse(null);
                if (rider == null) {
                    System.err.println("Cannot trigger state machine for rider " + riderSessionId + ": Rider not found in registry.");
                    continue; // Skip this ride
                }
                // Store original request if not already done
                if(rider.getOriginalRequest() == null) {
                    rider.setOriginalRequest(ride);
                }


                StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(riderSessionId);
                if (stateMachine == null) {
                    stateMachine = rideStateMachineService.createStateMachine(riderSessionId);
                    // Need to set rider session ID and ride ID in context here too
                    String rideId = "batchride-" + riderSessionId + "-" + System.currentTimeMillis(); // Generate an ID for the batch instance
                    RideStateMachineConfig.toContext(stateMachine, RiderService.RIDE_ID_KEY, rideId); // Use constant
                    RideStateMachineConfig.toContext(stateMachine, RiderService.RIDER_SESSION_ID_KEY, riderSessionId); // Use constant

                } else {
                    // If SM exists, ensure it's in a state ready for triggering (e.g., INITIATED)
                    if (stateMachine.getState().getId() != RideState.INITIATED) {
                        System.err.println("State machine for rider " + riderSessionId + " exists but is not in INITIATED state ("+ stateMachine.getState().getId() +"). Cannot trigger batch event.");
                        continue; // Skip triggering if SM is in an unexpected state
                    }
                }

                // Add computed batch route to context
                RideStateMachineConfig.toContext(stateMachine, EventProcessor.BATCH_ROUTE_KEY, mergedRoute); // Use constant

                // Trigger the event
                System.out.println("Triggering SCHEDULED_RIDE_TRIGGERED for rider: " + riderSessionId);
                stateMachine.sendEvent(RideEvent.SCHEDULED_RIDE_TRIGGERED);

            } catch (Exception e) {
                System.err.println("Error triggering state machine for batched ride (rider: " + riderSessionId + "): " + e.getMessage());
                // Consider how to handle individual trigger failures
            }
        }
    }

} // End of RideBatchScheduler
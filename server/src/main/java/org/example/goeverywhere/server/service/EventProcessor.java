// src/main/java/org/example/goeverywhere/server/service/EventProcessor.java
package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;

import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.UserRegistry.Driver;
import org.example.goeverywhere.server.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.fromContext;
import static org.example.goeverywhere.server.flow.RideStateMachineConfig.toContext;
import static org.example.goeverywhere.server.service.RiderService.*;

import org.example.goeverywhere.protocol.grpc.Route; // Ensure Route is imported
import org.example.goeverywhere.protocol.grpc.RiderEvent; // Ensure RiderEvent is imported
import org.example.goeverywhere.protocol.grpc.DriverEvent; // Ensure DriverEvent is imported
import org.example.goeverywhere.protocol.grpc.RideRegistered; // Ensure RideRegistered is imported
import org.example.goeverywhere.protocol.grpc.RideRequested; // Ensure RideRequested is imported
import org.example.goeverywhere.server.service.RideBatchScheduler;

@Service
public class EventProcessor {

    public static final String DRIVER_SESSION_ID_KEY = "driverSessionId";
    public static final String BATCH_ROUTE_KEY = "computedRoute"; // Key used by RideBatchScheduler
    public static final String ASSIGNED_ROUTE_KEY = "assignedRoute"; // Use a distinct key after assignment

    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired // Inject the batch scheduler for re-queueing
    private RideBatchScheduler rideBatchScheduler;

    @Autowired
    @Qualifier("optimizedRouteService") // Assuming this is the correct qualifier
    private RouteService routeService; // Keep using RouteService interface

    @Autowired
    private DriverLocationUpdateService driverLocationUpdateService;

    private final ExecutorService deferredEventsExecutor = Executors.newFixedThreadPool(4);

    // --- Action for Immediate Ride Request (Mostly Unchanged) ---
    public Action<RideState, RideEvent> requestRide() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY); // Assuming rideId is set before this

            UserRegistry.Rider rider = getRider(riderSessionId);
            LatLng riderLocation = rider.getOrigin();
            LatLng destinationLocation = rider.getDestination();

            // Generate initial route for this rider
            Route initialRoute = routeService.generateRoute(riderLocation, destinationLocation, riderSessionId);

            // Find driver and potentially merge/optimize dynamically
            Optional<Pair<UserRegistry.Driver, Route>> driverRoutePairOpt = userRegistry.findAvailableDriverAndNewRoute(initialRoute, rideId);

            if (driverRoutePairOpt.isEmpty()) {
                System.out.println("No available driver found dynamically for rideId: " + rideId);
                sendEventToStateMachineDeferred(context.getStateMachine(), RideEvent.NO_AVAILABLE_DRIVERS);
                return;
            }

            Pair<UserRegistry.Driver, Route> driverRoutePair = driverRoutePairOpt.get();
            UserRegistry.Driver driver = driverRoutePair.getFirst();
            Route assignedRoute = driverRoutePair.getSecond(); // This is the potentially merged route

            // Notify rider with their segment (using improved getRouteSegment eventually)
            Route riderSegment = routeService.getRouteSegment(assignedRoute, riderLocation, destinationLocation);
            rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                    .setRideRegistered(RideRegistered.newBuilder()
                            .setNewRoute(riderSegment)) // Send segment
                    .build());

            // Store assigned route and driver in context
            toContext(context, ASSIGNED_ROUTE_KEY, assignedRoute); // Use ASSIGNED_ROUTE_KEY
            toContext(context, DRIVER_SESSION_ID_KEY, driver.getSessionId());

            // Notify the selected driver
            StreamObserver<DriverEvent> streamObserver = driver.getStreamObserver();
            streamObserver.onNext(DriverEvent.newBuilder()
                    .setRideRequested(RideRequested.newBuilder() // Use RideRequested
                            .setRiderId(riderSessionId) // Pass rider ID
                            .setNewRoute(assignedRoute)) // Send the full assigned route
                    .build());

            // State transition to REQUESTED happens automatically by the state machine framework
        };
    }

    // --- *** NEW ACTION for Assigning Batched Rides *** ---
    public Action<RideState, RideEvent> assignBatchedRideToDriver() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY); // Should be set when SM created
            Route batchRoute = fromContext(context, BATCH_ROUTE_KEY); // Get the route from BatchScheduler

            if (batchRoute == null || batchRoute.getWaypointsCount() == 0) {
                System.err.println("Batch route missing or empty for rider: " + riderSessionId);
                sendEventToStateMachineDeferred(context.getStateMachine(), RideEvent.NO_AVAILABLE_DRIVERS); // Or a different error event
                return;
            }

            UserRegistry.Rider rider = getRider(riderSessionId); // Ensure rider exists

            // --- Core Logic: Find a driver for this pre-calculated batch route ---
            // This requires a new method in UserRegistry
            Optional<UserRegistry.Driver> driverOpt = userRegistry.findAvailableDriverForBatchRoute(batchRoute);

            if (driverOpt.isEmpty()) {
                System.out.println("No available driver found for pre-calculated batch route for rider: " + riderSessionId);
                // Handle this failure - maybe retry later, cancel, or notify admin?
                // For now, send NO_AVAILABLE_DRIVERS for this specific rider.
                sendEventToStateMachineDeferred(context.getStateMachine(), RideEvent.NO_AVAILABLE_DRIVERS);
                return;
            }

            UserRegistry.Driver driver = driverOpt.get();
            System.out.println("Assigning batch route to driver " + driver.getSessionId() + " for rider " + riderSessionId);

            // Store assigned driver and the route in context
            toContext(context, DRIVER_SESSION_ID_KEY, driver.getSessionId());
            toContext(context, ASSIGNED_ROUTE_KEY, batchRoute); // Store the route under a consistent key

            // Notify the assigned driver about the full batch route
            driver.getStreamObserver().onNext(DriverEvent.newBuilder()
                    .setRideRequested(RideRequested.newBuilder() // Reuse RideRequested or create a specific BatchAssigned event
                            .setRiderId(riderSessionId) // Associate with one rider initially? Or use a batch ID?
                            .setNewRoute(batchRoute))
                    .build());

            // Notify the rider about their registration and segment
            Route riderSegment = routeService.getRouteSegment(batchRoute, rider.getOrigin(), rider.getDestination());
            rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                    .setRideRegistered(RideRegistered.newBuilder()
                            .setNewRoute(riderSegment))
                    .build());

            // State transition to REQUESTED happens automatically by the state machine framework
        };
    }


    // --- driverAccepted Action (Adjusted to use ASSIGNED_ROUTE_KEY) ---
    public Action<RideState, RideEvent> driverAccepted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            UserRegistry.Rider rider = getRider(riderSessionId);
            Driver driver = getDriver(driverSessionId);

            // Retrieve the route that was assigned and accepted
            Route assignedRoute = fromContext(context, ASSIGNED_ROUTE_KEY); // Use the consistent key
            if (assignedRoute == null) {
                System.err.println("CRITICAL: Assigned route missing in context during driver acceptance for rider: " + riderSessionId);
                // Handle error appropriately - maybe cancel ride
                sendEventToStateMachineDeferred(context.getStateMachine(), RideEvent.NO_AVAILABLE_DRIVERS); // Reusing event for now
                return;
            }

            // Assign route to driver state
            driver.getCurrentRideIds().add(riderSessionId); // Assuming this handles uniqueness if called multiple times for batch
            driver.setCurrentFullRoute(assignedRoute); // Set the full (potentially batch) route

            // Calculate route segment to the *first* pickup for this specific rider
            Route routeToOrigin = routeService.getRouteSegment(assignedRoute, driver.location, rider.getOrigin());

            // Notify driver with full details (might be redundant if sent before, but confirms acceptance)
            driver.getStreamObserver().onNext(DriverEvent.newBuilder()
                    .setRideDetails(RideDetails.newBuilder().setNewFullRoute(assignedRoute))
                    .build());

            // Notify the accepting rider
            RideAccepted.Builder rideAccepted = RideAccepted.newBuilder().setRouteToRider(routeToOrigin);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideAccepted(rideAccepted).build());


            // Update *other* riders already on this driver's potentially merged route
            for (String otherRiderId : driver.getCurrentRideIds()) {
                // Skip the rider who just got accepted
                if(Objects.equals(otherRiderId, riderSessionId)) {
                    continue;
                }

                userRegistry.getRiderMaybe(otherRiderId).ifPresent(otherRider -> {
                    // Determine the next relevant waypoint for the other rider (their pickup or dest)
                    LatLng nextWaypointForOtherRider = otherRider.isPickedUp ? otherRider.getDestination() : otherRider.getOrigin();

                    // Get the updated segment for the other rider from the driver's current location
                    Route updatedSegment = routeService.getRouteSegment(assignedRoute, driver.location, nextWaypointForOtherRider);
                    otherRider.setCurrentRoute(updatedSegment); // Update rider state

                    // Notify the other rider
                    RouteUpdated.Builder routeUpdated = RouteUpdated.newBuilder().setNewRoute(updatedSegment);
                    sendEventToRider(otherRiderId, RiderEvent.newBuilder().setRouteUpdated(routeUpdated).build());
                });
            }

            // Send internal event to move state machine forward
            sendEventToStateMachineDeferred(context.getStateMachine(), RideEvent.DRIVER_EN_ROUTE); // Use deferred helper
            driverLocationUpdateService.startLocationUpdates(driverSessionId, riderSessionId); // Link update stream
        };
    }

    // --- driverRejected Action (Consider implications for batched rides) ---
    public Action<RideState, RideEvent> driverRejected() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY); // Unique ID for the ride instance

            // Mark rejection by this driver
            if (driverSessionId != null) {
                Driver driver = getDriver(driverSessionId);
                if (rideId != null) {
                    driver.addRejectedRide(rideId);
                    System.out.println("Driver " + driverSessionId + " rejected rideId " + rideId);
                } else {
                    System.err.println("Ride ID missing in context for rejection by driver " + driverSessionId);
                    // Cannot track rejection accurately without rideId
                }
                // Make driver available again if they were assigned this rejected ride
                // Check if this was their only ride before setting available
                if (driver.getCurrentRideIds().isEmpty() && driver.getCurrentFullRoute() == null) {
                    driver.setAvailable(true);
                }

            } else {
                System.err.println("Driver session ID missing during rejection for rider " + riderSessionId);
            }

            // Notify rider
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setDriverRejected(DriverRejected.newBuilder()
                            .setRideId(Objects.toString(rideId, "unknown")))
                    .build());

            // --- Rejection Handling Logic ---
            boolean wasBatched = context.getExtendedState().getVariables().containsKey(BATCH_ROUTE_KEY);

            if (wasBatched) {
                System.out.println("Handling rejection for a batched ride assignment for rider: " + riderSessionId);
                // Option B: Re-queue the rider
                UserRegistry.Rider rider = getRider(riderSessionId); // Get rider details
                RideRequest originalRequest = rider.getOriginalRequest(); // Get stored request

                if (originalRequest != null) {
                    try {
                        rideBatchScheduler.addRide(originalRequest); // Re-add to pending list
                        System.out.println("Re-queued rejected batched ride for rider: " + riderSessionId);
                        // Notify rider about re-queueing attempt
                        sendEventToRider(riderSessionId, RiderEvent.newBuilder().setSystemCancelled(
                                SystemCancelled.newBuilder().setMessage("Assigned driver unavailable. We'll try assigning again soon.")
                        ).build());
                        // Transition to CANCELLED state for this *attempt*, but rider is re-queued
                        context.getStateMachine().sendEvent(RideEvent.NO_AVAILABLE_DRIVERS); // Use existing event to reach CANCELLED


                    } catch (Exception e) {
                        System.err.println("Failed to re-queue rider " + riderSessionId + ": " + e.getMessage());
                        // Fallback: Cancel if re-queue fails
                        context.getStateMachine().sendEvent(RideEvent.NO_AVAILABLE_DRIVERS);
                    }
                } else {
                    System.err.println("Cannot re-queue rider " + riderSessionId + ": Original RideRequest not found.");
                    // Fallback: Cancel
                    context.getStateMachine().sendEvent(RideEvent.NO_AVAILABLE_DRIVERS);
                }
                // **Do NOT clean up rider resources here if re-queued**
                // The CANCELLED state handler might call cleanUpRider, need to adjust cleanUpRider maybe?
                // Or introduce a REQUEUED terminal state?
                // For now, relying on CANCELLED transition + NO cleanup if requeued worked.

            } else {
                // Immediate Ride Rejection
                System.out.println("Handling rejection for an immediate ride request for rider: " + riderSessionId);
                // Option A: Retry finding another driver immediately
                // Add a retry counter to context? For now, just retry once.
                int retryCount = context.getExtendedState().get("retryCount", Integer.class) == null ? 0 : context.getExtendedState().get("retryCount", Integer.class);
                if (retryCount < 1) { // Limit retries
                    System.out.println("Retrying driver search for immediate ride: " + riderSessionId);
                    toContext(context, "retryCount", retryCount + 1);
                    context.getStateMachine().sendEvent(RideEvent.RIDE_REQUESTED); // Trigger search again
                } else {
                    System.out.println("Max retries reached for immediate ride: " + riderSessionId + ". Cancelling.");
                    context.getStateMachine().sendEvent(RideEvent.NO_AVAILABLE_DRIVERS); // Cancel after retries
                }

            }
            // State transition happens based on event sent (NO_AVAILABLE_DRIVERS or RIDE_REQUESTED)
        };
    }

    // --- Other actions (driverArrived, rideStarted, rideCompleted, noAvailableDrivers) - Likely okay, but review context keys ---
    // Ensure they use ASSIGNED_ROUTE_KEY if they need the route.

    public Action<RideState, RideEvent> driverArrived() {
        // ... (likely okay) ...
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            Driver driver = getDriver(driverSessionId);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setDriverArrived(DriverArrived.newBuilder().setLocation(driver.location).build()).build());
            // Event DRIVER_ARRIVED sent externally by DriverService, transition happens automatically
        };
    }

    public Action<RideState, RideEvent> rideStarted() {
        // ... (ensure ASSIGNED_ROUTE_KEY is used if needed) ...
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            Driver driver = getDriver(driverSessionId);
            UserRegistry.Rider rider = getRider(riderSessionId);
            Route assignedRoute = fromContext(context, ASSIGNED_ROUTE_KEY); // Get the assigned route

            if (assignedRoute == null) {
                System.err.println("Assigned route missing in context during ride start for rider: " + riderSessionId);
                // Handle error - perhaps cannot start ride
                return;
            }

            rider.setPickedUp(true);
            // Calculate segment from current location to rider's destination using the assigned route
            Route routeToDestination = routeService.getRouteSegment(assignedRoute, driver.location, rider.destination);
            rider.setCurrentRoute(routeToDestination); // Update rider's view

            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideStarted(RideStarted.newBuilder()
                    // Potentially include routeToDestination in RideStarted message if needed by client
                    .setRouteToDestination(routeToDestination) // Added routeToDestination to proto message
                    .build()).build());
            // Event RIDE_STARTED sent externally by DriverService, transition happens automatically
        };
    }

    public Action<RideState, RideEvent> rideCompleted() {
        // ... (ensure cleanup logic is robust for batches) ...
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY); // Get driver to update state

            driverLocationUpdateService.stopLocationUpdates(riderSessionId); // Stop updates for this rider

            // Notify rider
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideCompleted(RideCompleted.newBuilder()
                    // Add fare calculation if applicable
                    .build()).build());

            // Update Driver state: remove rider, check if route completed
            if (driverSessionId != null) {
                userRegistry.getDriverMaybe(driverSessionId).ifPresent(driver -> {
                    driver.getCurrentRideIds().remove(riderSessionId);
                    // If no more riders, clear the route? Or does external completion handle this?
                    if (driver.getCurrentRideIds().isEmpty()) {
                        driver.setCurrentFullRoute(null); // Clear route when last rider dropped off
                        // Potentially update driver availability status
                    }
                    System.out.println("Rider " + riderSessionId + " completed ride with driver " + driverSessionId);
                });
            } else {
                System.err.println("Driver session ID missing during ride completion for rider: " + riderSessionId);
            }


            // Clean up rider resources
            cleanUpRider(riderSessionId);
            // Event RIDE_COMPLETED sent externally by DriverService, transition happens automatically
        };
    }

    public Action<RideState, RideEvent> noAvailableDrivers() {
        // ... (mostly okay, ensures cleanup) ...
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY); // Use RIDER_SESSION_ID_KEY
            System.out.println("No available drivers final state for rider: " + riderSessionId);
            userRegistry.getRiderMaybe(riderSessionId).ifPresent(rider -> {
                StreamObserver<RiderEvent> streamObserver = rider.getStreamObserver();
                streamObserver.onNext(RiderEvent.newBuilder()
                        .setSystemCancelled(SystemCancelled.newBuilder()
                                .setMessage("No available drivers found for your request.")
                                .build())
                        .build());
            });
            cleanUpRider(riderSessionId);
            // Transition to CANCELLED happens automatically
        };
    }

    // --- Helper Methods ---

    private void cleanUpRider(String riderSessionId) {
        userRegistry.getRiderMaybe(riderSessionId).ifPresent(rider -> {
            StreamObserver<RiderEvent> streamObserver = rider.getStreamObserver();
            try {
                if (streamObserver != null) {
                    streamObserver.onCompleted();
                }
            } catch (Exception e) {
                System.err.println("Error closing rider stream observer for " + riderSessionId + ": " + e.getMessage());
            } finally {
                userRegistry.unregisterRider(riderSessionId);
                System.out.println("Cleaned up resources for rider: " + riderSessionId);
            }
        });
        // Also ensure state machine associated with this rider is stopped/destroyed
        rideStateMachineService.stopStateMachine(riderSessionId);
    }


    private UserRegistry.Rider getRider(String riderSessionId) {
        return userRegistry.getRiderMaybe(riderSessionId)
                .orElseThrow(() -> new IllegalStateException("Rider session not found: " + riderSessionId));
    }

    private Driver getDriver(String driverSessionId) {
        return userRegistry.getDriverMaybe(driverSessionId)
                .orElseThrow(() -> new IllegalStateException("Driver session not found: " + driverSessionId));
    }

    private void sendEventToRider(String riderSessionId, RiderEvent rideEvent) {
        try {
            getRider(riderSessionId).getStreamObserver().onNext(rideEvent);
        } catch (IllegalStateException e) {
            System.err.println("Failed to send event to rider " + riderSessionId + ": Rider not found.");
        } catch (Exception e) {
            System.err.println("Failed to send event to rider " + riderSessionId + ": " + e.getMessage());
            // Consider cleanup if stream is broken
            // cleanUpRider(riderSessionId);
        }
    }

    // Helper to send event asynchronously to prevent deadlocks during transitions
    private void sendEventToStateMachineDeferred(StateMachine<RideState, RideEvent> stateMachine, RideEvent rideEvent) {
        deferredEventsExecutor.submit(() -> {
            try {
                // Check if state machine is still valid/running before sending event
                if (stateMachine != null /* && !stateMachine.isComplete() */) { // Add check if SM can complete prematurely
                    System.out.println("Deferring event " + rideEvent + " to state machine for rider: " + fromContext(stateMachine, RIDER_SESSION_ID_KEY));
                    stateMachine.sendEvent(rideEvent);
                } else {
                    System.err.println("State machine is null or completed, cannot send deferred event: " + rideEvent);
                }
            } catch (Exception e) {
                System.err.println("Error sending deferred event " + rideEvent + " to state machine: " + e.getMessage());
            }
        });
    }

    // Remove the old interceptor-based sendEventToStateMachine if not needed
    /*
    private void sendEventToStateMachine(String riderId, RideEvent rideEvent) {
        StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(riderId);
        if (stateMachine == null) {
             System.err.println("State machine not found for riderId=" + riderId + " when trying to send event " + rideEvent);
             return;
        }
        // The interceptor approach can be complex and might cause issues.
        // Prefer using the deferred executor approach.
        sendEventToStateMachineDeferred(stateMachine, rideEvent);
    }
    */

    @PreDestroy
    public void shutdownExecutor() {
        System.out.println("Shutting down EventProcessor deferred executor...");
        deferredEventsExecutor.shutdown();
        try {
            if (!deferredEventsExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                deferredEventsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            deferredEventsExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("EventProcessor deferred executor shut down.");
    }
}
package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideStateMachineConfig;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.routing.OptimizedRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class ScheduledRideService {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private OptimizedRouteService optimizedRouteService;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private UserRegistry userRegistry;

    /**
     * Schedule a ride that is meant for a future pickup.
     *
     * @param request the ride request message (must include a scheduled pickup time)
     * @param scheduledPickupTime the time when the ride should be processed (as an Instant)
     */
    public void scheduleRide(RideRequest request, Instant scheduledPickupTime) {
        // Schedule a task to trigger processing at the scheduled time.
        taskScheduler.schedule(() -> processScheduledRide(request),
                Date.from(scheduledPickupTime));
    }

    private void processScheduledRide(RideRequest request) {
        try {
            // Decode addresses
            LatLng origin = geocodingService.decodeAddress(request.getOrigin());
            LatLng destination = geocodingService.decodeAddress(request.getDestination());
            String riderSessionId = request.getSessionId();

            // Retrieve (or create) the state machine for this ride.
            var stateMachine = rideStateMachineService.getStateMachine(riderSessionId);
            if (stateMachine == null) {
                stateMachine = rideStateMachineService.createStateMachine(riderSessionId);
            }

            // Generate the optimal route using the optimized route service.
            Route route = optimizedRouteService.generateRoute(origin, destination, riderSessionId);

            // Inject the computed route into the state machine context.
            RideStateMachineConfig.toContext(stateMachine, "computedRoute", route);

            // Trigger a new event to start processing the scheduled ride.
            // (Make sure to add SCHEDULED_RIDE_TRIGGERED in your RideEvent enum.)
            stateMachine.sendEvent(RideEvent.SCHEDULED_RIDE_TRIGGERED);
        } catch (Exception e) {
            System.err.println("Failed to process scheduled ride: " + e.getMessage());
        }
    }
}

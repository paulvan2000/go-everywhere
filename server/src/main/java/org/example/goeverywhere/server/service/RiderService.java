package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineConfig;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.example.goeverywhere.server.service.ScheduledRideService;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.toContext;

import java.time.Instant;

@Service
public class RiderService {

    public static final String RIDER_SESSION_ID_KEY = "riderSessionId";
    public static final String NEW_ROUTE_CANDIDATE_KEY = "newRouteCandidate";
    public static final String RIDE_ID_KEY = "rideId";

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private org.example.goeverywhere.server.service.RideBatchScheduler rideBatchScheduler;  // Inject our new batch scheduler

    @Autowired
    private UserRegistry userRegistry;

    public void requestRide(RideRequest request) {
        try {
            LatLng origin = geocodingService.decodeAddress(request.getOrigin());
            LatLng destination = geocodingService.decodeAddress(request.getDestination());
            String riderSessionId = request.getSessionId();

            // In proto3, if scheduled_pickup_time is not set, getScheduledPickupTime() returns 0.
            // Check if scheduled pickup time is > 0 and at least 2 hours in the future.
            long scheduledTime = request.getScheduledPickupTime();
            if (scheduledTime > 0 && scheduledTime > Instant.now().plusSeconds(2 * 3600).getEpochSecond()) {
                // Ride is scheduled well in advance; add it to the batch.
                rideBatchScheduler.addRide(request);
            } else {
                // Process immediate ride as usual.
                String rideId = generateRideId(request);
                StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.createStateMachine(riderSessionId);
                RideStateMachineConfig.toContext(stateMachine, RIDE_ID_KEY, rideId);
                RideStateMachineConfig.toContext(stateMachine, RIDER_SESSION_ID_KEY, riderSessionId);

                UserRegistry.Rider rider = userRegistry.getRiderMaybe(riderSessionId).orElseThrow();
                rider.setOrigin(origin);
                rider.setDestination(destination);
                stateMachine.sendEvent(RideEvent.RIDE_REQUESTED);
            }
        } catch (Exception e) {
            System.err.println("Failed to process ride request: " + e.getMessage());
        }
    }

    private String generateRideId(RideRequest request) {
        return "ride-" + request.getSessionId() + "-" + System.currentTimeMillis();
    }
}

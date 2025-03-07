package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.toContext;

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
    private UserRegistry userRegistry;

    public void requestRide(RideRequest request) {
        try {
            LatLng origin = geocodingService.decodeAddress(request.getOrigin());
            LatLng destination = geocodingService.decodeAddress(request.getDestination());

            System.out.println("Decoded Origin: " + origin);
            System.out.println("Decoded Destination: " + destination);

            String riderSessionId = request.getSessionId();

            StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.createStateMachine(riderSessionId);

            toContext(stateMachine, RIDER_SESSION_ID_KEY, riderSessionId);


            UserRegistry.Rider rider = userRegistry.getRiderMaybe(riderSessionId).orElseThrow();
            rider.setOrigin(origin);
            rider.setDestination(destination);

            stateMachine.sendEvent(RideEvent.RIDE_REQUESTED);
        } catch (Exception e) {
            // Handle errors (e.g., geocoding failures)
            System.err.println("Failed to process ride request: " + e.getMessage());
        }
    }

}

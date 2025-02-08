package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.RideRequested;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.SystemCancelled;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.fromContext;

@Service
public class RiderService {

    public static final String RIDER_SESSION_ID_KEY = "riderSessionId";
    public static final String RIDE_ID_KEY = "rideId";
    public static final String ORIGIN_KEY = "origin";
    public static final String DESTINATION_KEY = "destination";
    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private UserRegistry userRegistry;

    public void requestRide(RideRequest request) {
        try {
            LatLng originCoordinates = geocodingService.decodeAddress(request.getOrigin());
            LatLng destinationCoordinates = geocodingService.decodeAddress(request.getDestination());

            System.out.println("Decoded Origin: " + originCoordinates);
            System.out.println("Decoded Destination: " + destinationCoordinates);

            String rideId = generateRideId(request);

            StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.createStateMachine(rideId);

            stateMachine.getExtendedState().getVariables().put(RIDE_ID_KEY, rideId);
            String riderSessionId = request.getSessionId();
            stateMachine.getExtendedState().getVariables().put(RIDER_SESSION_ID_KEY, riderSessionId);
            stateMachine.getExtendedState().getVariables().put(ORIGIN_KEY, originCoordinates);
            stateMachine.getExtendedState().getVariables().put(DESTINATION_KEY, destinationCoordinates);

            stateMachine.sendEvent(RideEvent.RIDE_REQUESTED);
        } catch (Exception e) {
            // Handle errors (e.g., geocoding failures)
            System.err.println("Failed to process ride request: " + e.getMessage());
        }
    }

    private String generateRideId(RideRequest request) {
        return "ride-" + request.getSessionId() + "-" + System.currentTimeMillis();
    }
}

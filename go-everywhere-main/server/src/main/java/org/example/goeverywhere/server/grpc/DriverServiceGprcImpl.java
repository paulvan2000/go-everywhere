package org.example.goeverywhere.server.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.example.goeverywhere.server.service.EventProcessor.DRIVER_SESSION_ID_KEY;

@Service
public class DriverServiceGprcImpl extends DriverServiceGrpc.DriverServiceImplBase {

    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Override
    public void subscribeForRideEvents(SubscribeForRideEventsRequest request, StreamObserver<DriverEvent> responseObserver) {
        userRegistry.registerDriver(request.getSessionId(), responseObserver);
    }

    @Override
    public void acceptRide(AcceptRideRequest request, StreamObserver<Empty> responseObserver) {
        validateIdentity(request.getSessionId(), request.getRiderId());
        rideStateMachineService.sendEvent(request.getRiderId(), RideEvent.DRIVER_ACCEPTED);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void rejectRide(RejectRideRequest request, StreamObserver<Empty> responseObserver) {
        validateIdentity(request.getSessionId(), request.getRiderId());
        rideStateMachineService.sendEvent(request.getRiderId(), RideEvent.DRIVER_REJECTED);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void driverArrived(DriverArrivedRequest request, StreamObserver<Empty> responseObserver) {
        String driverSessionId = request.getSessionId();
        String riderSessionId = request.getRiderId(); // The sessionId of the rider

        // Validate that this driver is associated with the triggering ride
        validateIdentity(driverSessionId, riderSessionId);

        rideStateMachineService.sendEvent(riderSessionId, RideEvent.DRIVER_ARRIVED);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void rideStarted(RideStartedRequest request, StreamObserver<Empty> responseObserver) {
        validateIdentity(request.getSessionId(), request.getRiderId());
        rideStateMachineService.sendEvent(request.getRiderId(), RideEvent.RIDE_STARTED);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void rideCompleted(RideCompletedRequest request, StreamObserver<Empty> responseObserver) {
        // Now uses rider_id consistently
        validateIdentity(request.getSessionId(), request.getRiderId());
        rideStateMachineService.sendEvent(request.getRiderId(), RideEvent.RIDE_COMPLETED); // Use rider_id
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private void validateIdentity(String driverSessionId, String associatedRiderId) {
        Objects.requireNonNull(driverSessionId);
        Objects.requireNonNull(associatedRiderId);
        StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(associatedRiderId); // Use rider ID to get SM
        if (stateMachine == null) {
            throw new RuntimeException("State machine not found for riderId: " + associatedRiderId);
        }
        Object storedDriverId = stateMachine.getExtendedState().getVariables().get(DRIVER_SESSION_ID_KEY);
        if (storedDriverId == null || !storedDriverId.equals(driverSessionId)) {
            throw new RuntimeException("Invalid driver session id (" + driverSessionId + ") for ride associated with rider " + associatedRiderId);
        }
    }
}

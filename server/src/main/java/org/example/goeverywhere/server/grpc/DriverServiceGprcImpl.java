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

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.fromContext;
import static org.example.goeverywhere.server.service.DriverService.DRIVER_SESSION_ID_KEY;

@Service
public class DriverServiceGprcImpl extends DriverServiceGrpc.DriverServiceImplBase {

    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Override
    public void subscribeForRideEvents(SubscribeForRideEvents request, StreamObserver<DriverEvent> responseObserver) {
        userRegistry.registerDriver(request.getSessionId(), responseObserver);
    }


    @Override
    public void acceptRide(AcceptRideRequest request, StreamObserver<Empty> responseObserver) {
        validateIdentity(request.getSessionId(), request.getRideId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
        rideStateMachineService.sendEvent(request.getRideId(), RideEvent.DRIVER_ACCEPTED);
    }

    @Override
    public void driverArrived(DriverArrivedRequest request, StreamObserver<Empty> responseObserver) {
        validateIdentity(request.getSessionId(), request.getRideId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
        rideStateMachineService.sendEvent(request.getRideId(), RideEvent.DRIVER_ARRIVED);
    }

    private void validateIdentity(String sessionId, String riderId) {
        Objects.requireNonNull(sessionId);
        Objects.requireNonNull(riderId);
        StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(riderId);
        if(!stateMachine.getExtendedState().getVariables().get(DRIVER_SESSION_ID_KEY).equals(sessionId)) {
            throw new RuntimeException("Invalid session id");
        };
    }
}

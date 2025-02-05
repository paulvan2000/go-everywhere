package org.example.goeverywhere.server.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void acceptRide(AcceptRideRequest request, StreamObserver<AcceptRideResponse> responseObserver) {
        rideStateMachineService.sendEvent(request.getRideId(), RideEvent.DRIVER_ACCEPTED);

        responseObserver.onNext(AcceptRideResponse.newBuilder()
                .setRoute("Here goes a route definition to the user location")
                .build());

        // TODO: here we need a scheduled regular driver location updates sent to a user

        responseObserver.onCompleted();
    }
}

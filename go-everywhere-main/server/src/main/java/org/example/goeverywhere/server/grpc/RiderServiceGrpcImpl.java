package org.example.goeverywhere.server.grpc;

import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.RiderServiceGrpc;
import org.example.goeverywhere.server.service.RiderService;
import org.example.goeverywhere.server.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiderServiceGrpcImpl extends RiderServiceGrpc.RiderServiceImplBase {

    @Autowired
    private RiderService riderService;
    @Autowired
    private UserRegistry userRegistry;

    @Override
    public void requestRide(RideRequest request, StreamObserver<RiderEvent> responseObserver) {
        userRegistry.registerRider(request.getSessionId(), responseObserver);
        riderService.requestRide(request);
    }
}

package org.example.goeverywhere.server.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.exceptions.FailedAuthenticationException;
import org.example.goeverywhere.server.service.UserRegistry;
import org.example.goeverywhere.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * gRPC implementation for handling login operations.
 */
@Service
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRegistry userRegistry;

    /**
     * Handles user login requests.
     *
     * @param request the login request containing user credentials.
     * @param responseObserver the response observer to return the login response.
     */
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        System.out.println("Request received from client:\n" + request);
        try {
            LoginResponse response = userService.login(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            System.out.println("Successfully authenticated:\n" + request.getEmail());
        } catch (FailedAuthenticationException e) {
            // Invalid credentials
            System.err.println("Failed to authenticate:\n" + request.getEmail());
            responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Invalid credentials").asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown server error").asRuntimeException());
        }
    }

    /**
     * Handles user sign-up requests.
     *
     * @param request the sign-up request containing user details.
     * @param responseObserver the response observer to confirm the sign-up.
     */
    @Override
    public void signUp(SignUpRequest request, StreamObserver<Empty> responseObserver) {
        System.out.println("Request received from client:\n" + request);

        try {
            userService.signUp(request);
            System.out.println("User saved successfully.");
        } catch (Exception e) {
            if(e.getMessage().contains("[SQLITE_CONSTRAINT_UNIQUE]")) {
                responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
            } else{
                e.printStackTrace();
                responseObserver.onError(e);
            }
            return;
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateCurrentLocation(UpdateCurrentLocationRequest request, StreamObserver<Empty> responseObserver) {
        userRegistry.updateUserLocation(request.getSessionId(), request.getLocation());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }




}
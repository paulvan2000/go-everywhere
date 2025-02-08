package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideState;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.UserRegistry.Driver;
import org.example.goeverywhere.server.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.fromContext;
import static org.example.goeverywhere.server.service.RiderService.*;

@Service
public class EventProcessor {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    public static final String DRIVER_SESSION_ID_KEY = "driverSessionId";

    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    @Qualifier("mockRouteService")
    private RouteService routeService;

    @Autowired
    private DriverLocationUpdateService driverLocationUpdateService;

    /**
     * This method process "Request ride" event. A user sent a request for a ride,
     * now the system needs to find the closest driver that is not busy and send a request to him
     * @return
     */
    public Action<RideState, RideEvent> requestRide() {
        return context -> {
            LatLng riderLocation = fromContext(context, ORIGIN_KEY);
            LatLng destinationLocation = fromContext(context, DESTINATION_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);

            Optional<Driver> driverMaybe = userRegistry.findClosestAvailableDriver(riderLocation, rideId);
            if (driverMaybe.isEmpty()) {
                sendEventToStateMachine(rideId, RideEvent.NO_AVAILABLE_DRIVERS);
                return;
            }

            StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(rideId);
            stateMachine.getExtendedState().getVariables().put(DRIVER_SESSION_ID_KEY, driverMaybe.get().getSessionId());

            StreamObserver<DriverEvent> streamObserver = driverMaybe.get().getStreamObserver();
            streamObserver.onNext(DriverEvent.newBuilder()
                    .setRideRequested(RideRequested.newBuilder()
                            .setRideId(rideId)
                            .setPickupLocation(riderLocation)
                            .setDestination(destinationLocation)
                            .build())
                    .build());
        };
    }

    public Action<RideState, RideEvent> prepareRouteAndSend() {
        return context -> {
            LatLng riderLocation = fromContext(context, ORIGIN_KEY);
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);

            Driver driver = getDriver(driverSessionId);

            Route route = routeService.generateRoute(driver.location, riderLocation);
            RideAccepted.Builder rideAccepted = RideAccepted.newBuilder().setRouteToRider(route).setRideId(rideId);
            driver.getStreamObserver().onNext(DriverEvent.newBuilder().setRideAccepted(rideAccepted).build());
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideAccepted(rideAccepted).build());

            sendEventToStateMachine(rideId, RideEvent.DRIVER_EN_ROUTE);
            driverLocationUpdateService.startLocationUpdates(rideId, driverSessionId, riderSessionId);
        };
    }

    public Action<RideState, RideEvent> driverRejected() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);

            Driver driver = getDriver(driverSessionId);
            driver.addRejectedRide(rideId);

            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setDriverRejected(DriverRejected.newBuilder()
                    .setRideId(rideId).build())
                    .build());
            // attempting to restart event flow with another driver
            sendEventToStateMachine(rideId, RideEvent.RIDE_REQUESTED);
        };
    }

    public Action<RideState, RideEvent> driverArrived() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            LatLng destinationLocation = fromContext(context, DESTINATION_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);
            Driver driver = getDriver(driverSessionId);
            Route routeToDestination = routeService.generateRoute(driver.location, destinationLocation);
            RideDetails.Builder rideDetails = RideDetails.newBuilder().setRideId(rideId).setRouteToDestination(routeToDestination);
            driver.getStreamObserver().onNext(DriverEvent.newBuilder().setRideDetails(rideDetails).build());
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setDriverArrived(DriverArrived.newBuilder().setRideId(rideId).setLocation(driver.location).build()).build());
        };
    }

    public Action<RideState, RideEvent> rideStarted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideStarted(RideStarted.newBuilder().setRideId(rideId).build()).build());
        };
    }

    public Action<RideState, RideEvent> rideCompleted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);
            driverLocationUpdateService.stopLocationUpdates(rideId);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideCompleted(RideCompleted.newBuilder().setRideId(rideId).build()).build());
        };
    }

    public Action<RideState, RideEvent> noAvailableDrivers() {
        return context -> {
            String sessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            userRegistry.getRiderMaybe(sessionId).ifPresent(rider -> {
                rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                        .setSystemCancelled(SystemCancelled.newBuilder()
                                .setMessage("No available drivers")
                                .build())
                        .build());
            });

        };
    }

    private Driver getDriver(String driverSessionId) {
        Driver driver = userRegistry.getDriverMaybe(driverSessionId)
                .orElseThrow(() -> new RuntimeException("Driver session not found"));
        return driver;
    }


    private void sendEventToRider(String riderSessionId, RiderEvent rideEvent) {
        UserRegistry.Rider rider = userRegistry.getRiderMaybe(riderSessionId)
                .orElseThrow(() -> new RuntimeException("Rider session not found"));
        rider.getStreamObserver().onNext(rideEvent);
    }

    private void sendEventToStateMachine(String rideId, RideEvent rideEvent) {
        executor.submit(() -> {
            StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(rideId);
            stateMachine.sendEvent(rideEvent);
        });

    }

}


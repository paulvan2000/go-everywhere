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
import org.springframework.data.util.Pair;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.goeverywhere.server.flow.RideStateMachineConfig.fromContext;
import static org.example.goeverywhere.server.flow.RideStateMachineConfig.toContext;
import static org.example.goeverywhere.server.service.RiderService.*;

@Service
public class EventProcessor {

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

    private final ExecutorService deferredEventsExecutor = Executors.newFixedThreadPool(4);

    /**
     * This method process "Request ride" event. A user sent a request for a ride,
     * now the system needs to find the closest driver that is not busy and send a request to him.
     *
     */
    public Action<RideState, RideEvent> requestRide() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);

            UserRegistry.Rider rider = getRider(riderSessionId);
            LatLng riderLocation = rider.getOrigin();
            LatLng destinationLocation = rider.getDestination();

            Route route = routeService.generateRoute(riderLocation, destinationLocation, riderSessionId);

            Optional<Pair<Driver, Route>> driverRoutePairOpt = userRegistry.findAvailableDriverAndNewRoute(route, rideId);
            if (driverRoutePairOpt.isEmpty()) {
                sendEventToStateMachine(riderSessionId, RideEvent.NO_AVAILABLE_DRIVERS);
                return;
            }

            Pair<Driver, Route> driverRoutePair = driverRoutePairOpt.get();
            Driver driver = driverRoutePair.getFirst() ;
            Route newRoute = driverRoutePair.getSecond();

            // notifying a rider that the system got the request and came up with a route
            rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                    .setRideRegistered(RideRegistered.newBuilder()
            // user gets a route from his location to the final destination
                    .setNewRoute(routeService.getRouteSegment(newRoute, riderLocation, destinationLocation))).build());

            toContext(context, NEW_ROUTE_CANDIDATE_KEY, newRoute);
            toContext(context, DRIVER_SESSION_ID_KEY, driver.getSessionId());
            StreamObserver<DriverEvent> streamObserver = driver.getStreamObserver();
            streamObserver.onNext(DriverEvent.newBuilder()
                    .setRideRequested(RideRequested.newBuilder()
                            .setRiderId(riderSessionId)
                            .setNewRoute(newRoute)
                            .build())
                    .build());
        };
    }


    public Action<RideState, RideEvent> driverAccepted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            UserRegistry.Rider rider = getRider(riderSessionId);

            LatLng riderOrigin = rider.origin;
            // this a merged route
            Route newFullRoute = fromContext(context, NEW_ROUTE_CANDIDATE_KEY);
            Driver driver = getDriver(driverSessionId);
            driver.getCurrentRideIds().add(riderSessionId);
            driver.setCurrentFullRoute(newFullRoute);

            Route routeToOrigin = routeService.getRouteSegment(newFullRoute, driver.location, riderOrigin);
            RideAccepted.Builder rideAccepted = RideAccepted.newBuilder().setRouteToRider(routeToOrigin);

            driver.getStreamObserver().onNext(DriverEvent.newBuilder().setRideDetails(RideDetails.newBuilder().setNewFullRoute(newFullRoute)).build());
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideAccepted(rideAccepted).build());

            // update all the previously added riders with the new route
            for (String id : driver.getCurrentRideIds()) {
                if(Objects.equals(id, riderSessionId)) {
                    continue;
                }

                userRegistry.getRiderMaybe(id).ifPresentOrElse(r ->   {
                    LatLng currentRouteDestination;
                    if(r.isPickedUp) {
                        currentRouteDestination = r.destination;
                    } else {
                        currentRouteDestination = r.origin;
                    }

                    Route newRouteSegment = routeService.getRouteSegment(newFullRoute, driver.location, currentRouteDestination);
                    rider.setCurrentRoute(newRouteSegment);

                    RouteUpdated.Builder routeUpdated = RouteUpdated.newBuilder()
                            .setNewRoute(newRouteSegment);
                    sendEventToRider(id, RiderEvent.newBuilder().setRouteUpdated(routeUpdated).build());
                }, () -> System.err.println("Rider sessionId=" + id + " is invalid"));
            }

            sendEventToStateMachine(riderSessionId, RideEvent.DRIVER_EN_ROUTE);
            driverLocationUpdateService.startLocationUpdates(driverSessionId, riderSessionId);
        };
    }

    public Action<RideState, RideEvent> driverRejected() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            String rideId = getRider(riderSessionId).getSessionId();

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
            Driver driver = getDriver(driverSessionId);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setDriverArrived(DriverArrived.newBuilder().setLocation(driver.location).build()).build());
        };
    }

    public Action<RideState, RideEvent> rideStarted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            String driverSessionId = fromContext(context, DRIVER_SESSION_ID_KEY);
            Driver driver = getDriver(driverSessionId);
            UserRegistry.Rider rider = getRider(riderSessionId);
            rider.setPickedUp(true);
            Route routeToDestination = routeService.getRouteSegment(driver.getCurrentFullRoute(), driver.location, rider.destination);
            rider.setCurrentRoute(routeToDestination);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideStarted(RideStarted.newBuilder().build()).build());
        };
    }

    public Action<RideState, RideEvent> rideCompleted() {
        return context -> {
            String riderSessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            driverLocationUpdateService.stopLocationUpdates(riderSessionId);
            sendEventToRider(riderSessionId, RiderEvent.newBuilder().setRideCompleted(RideCompleted.newBuilder().build()).build());

            // clean up
            cleanUpRider(riderSessionId);
        };
    }

    private void cleanUpRider(String riderSessionId) {
        userRegistry.getRiderMaybe(riderSessionId).ifPresent(rider -> {
            StreamObserver<RiderEvent> streamObserver = rider.getStreamObserver();
            streamObserver.onCompleted();
            userRegistry.unregisterRider(riderSessionId);
        });
    }

    public Action<RideState, RideEvent> noAvailableDrivers() {
        return context -> {
            String sessionId = fromContext(context, RIDER_SESSION_ID_KEY);
            userRegistry.getRiderMaybe(sessionId).ifPresent(rider -> {
                StreamObserver<RiderEvent> streamObserver = rider.getStreamObserver();
                streamObserver.onNext(RiderEvent.newBuilder()
                        .setSystemCancelled(SystemCancelled.newBuilder()
                                .setMessage("No available drivers")
                                .build())
                        .build());
            });
            cleanUpRider(sessionId);
        };
    }


    private UserRegistry.Rider getRider(String riderSessionId) {
        return userRegistry.getRiderMaybe(riderSessionId)
                .orElseThrow(() -> new RuntimeException("Rider session not found"));
    }

    private Driver getDriver(String driverSessionId) {
        return userRegistry.getDriverMaybe(driverSessionId)
                .orElseThrow(() -> new RuntimeException("Driver session not found"));
    }


    private void sendEventToRider(String riderSessionId, RiderEvent rideEvent) {
        getRider(riderSessionId).getStreamObserver().onNext(rideEvent);
    }

    private void sendEventToStateMachine(String riderId, RideEvent rideEvent) {
        StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(riderId);
        stateMachine.getStateMachineAccessor().doWithAllRegions(accessor ->
                accessor.addStateMachineInterceptor(new StateMachineInterceptorAdapter<>() {
                    @Override
                    public StateContext<RideState, RideEvent> postTransition(StateContext<RideState, RideEvent> stateContext) {
                        // processing the event asynchronously to prevent deadlocks
                        deferredEventsExecutor.submit(() -> stateMachine.sendEvent(rideEvent));
                        return stateContext;
                    }
                }));
    }

}


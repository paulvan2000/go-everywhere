package org.example.goeverywhere.server.flow;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.DriverEvent;
import org.example.goeverywhere.protocol.grpc.RideRequested;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.SystemCancelled;
import org.example.goeverywhere.server.service.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.Optional;

import static org.example.goeverywhere.server.service.RiderService.*;

@Configuration
@EnableStateMachineFactory
public class RideStateMachineConfig extends EnumStateMachineConfigurerAdapter<RideState, RideEvent> {

    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Override
    public void configure(StateMachineStateConfigurer<RideState, RideEvent> states) throws Exception {
        states
                .withStates()
                .initial(RideState.INITIATED)
                .state(RideState.REQUESTED)
                .state(RideState.DRIVER_ASSIGNED)
                .state(RideState.PENDING_DRIVER_RESPONSE)
                .state(RideState.DRIVER_EN_ROUTE)
                .state(RideState.DRIVER_ARRIVED)
                .state(RideState.RIDER_ONBOARD)
                .state(RideState.IN_RIDE)
                .end(RideState.COMPLETED)
                .end(RideState.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<RideState, RideEvent> transitions) throws Exception {
        transitions
                // Processing a request for a ride
                .withExternal()
                .source(RideState.INITIATED)
                .target(RideState.REQUESTED)
                .event(RideEvent.RIDE_REQUESTED)
                .action(requestRide())
                .and()
                // Processing cancellation
                .withExternal()
                .source(RideState.INITIATED)
                .target(RideState.CANCELLED)
                .event(RideEvent.NO_AVAILABLE_DRIVERS)
                .action(noAvailableDrivers())
                .and()


                .withExternal().source(RideState.DRIVER_ASSIGNED).target(RideState.PENDING_DRIVER_RESPONSE).event(RideEvent.DRIVER_ASSIGNED)
                .and()
                .withExternal().source(RideState.PENDING_DRIVER_RESPONSE).target(RideState.DRIVER_EN_ROUTE).event(RideEvent.DRIVER_ACCEPTED)
                .and()
                .withExternal().source(RideState.PENDING_DRIVER_RESPONSE).target(RideState.REQUESTED).event(RideEvent.DRIVER_REJECTED)
                .and()
                .withExternal().source(RideState.DRIVER_EN_ROUTE).target(RideState.DRIVER_ARRIVED).event(RideEvent.DRIVER_ARRIVED)
                .and()
                .withExternal().source(RideState.DRIVER_ARRIVED).target(RideState.RIDER_ONBOARD).event(RideEvent.RIDER_ONBOARD)
                .and()
                .withExternal().source(RideState.RIDER_ONBOARD).target(RideState.IN_RIDE).event(RideEvent.RIDE_STARTED)
                .and()
                .withExternal().source(RideState.IN_RIDE).target(RideState.COMPLETED).event(RideEvent.RIDE_COMPLETED);
    }

    /**
     * This method process "Request ride" event. A user sent a request for a ride,
     * now the system needs to find the closest driver that is not busy and send a request to him
     * @return
     */
    private Action<RideState, RideEvent> requestRide() {
        return context -> {
            LatLng riderLocation = fromContext(context, ORIGIN_KEY);
            LatLng destinationLocation = fromContext(context, DESTINATION_KEY);
            String rideId = fromContext(context, RIDE_ID_KEY);

            Optional<UserRegistry.Driver> driverMaybe = userRegistry.findClosestAvailableDriver(riderLocation, rideId);
            if (driverMaybe.isEmpty()) {
                sendEvent(rideId, RideEvent.NO_AVAILABLE_DRIVERS);
                return;
            }

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

    private Action<RideState, RideEvent> noAvailableDrivers() {
        return context -> {
            String sessionId = fromContext(context, SESSION_ID_KEY);
            userRegistry.getRiderMaybe(sessionId).ifPresent(rider -> {
                rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                        .setSystemCancelled(SystemCancelled.newBuilder()
                                .setMessage("No available drivers")
                                .build())
                        .build());
            });

        };
    }

    private void sendEvent(String rideId, RideEvent rideEvent) {
        StateMachine<RideState, RideEvent> stateMachine = rideStateMachineService.getStateMachine(rideId);
        stateMachine.sendEvent(rideEvent);
    }

    private static <T> T fromContext(StateContext<RideState, RideEvent> context, String key) {
        return (T) context.getExtendedState().getVariables().get(key);
    }

}

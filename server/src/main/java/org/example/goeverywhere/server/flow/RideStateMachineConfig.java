package org.example.goeverywhere.server.flow;

import org.example.goeverywhere.server.service.EventProcessor;
import org.example.goeverywhere.server.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class RideStateMachineConfig extends EnumStateMachineConfigurerAdapter<RideState, RideEvent> {

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private RiderService riderService;

    @Override
    public void configure(StateMachineStateConfigurer<RideState, RideEvent> states) throws Exception {
        states
                .withStates()
                .initial(RideState.INITIATED)
                .state(RideState.REQUESTED)
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
                .event(RideEvent.RIDE_REQUESTED)
                .action(eventProcessor.requestRide())
                .target(RideState.REQUESTED)
                .and()
                // Processing cancellation
                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.NO_AVAILABLE_DRIVERS)
                .action(eventProcessor.noAvailableDrivers())
                .target(RideState.CANCELLED)
                .and()

                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.DRIVER_ACCEPTED)
                .action(eventProcessor.prepareRouteAndSend())
                .target(RideState.DRIVER_EN_ROUTE)


                .and()
                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.DRIVER_REJECTED)
                .action(eventProcessor.driverRejected())
                .target(RideState.INITIATED)

                .and()
                .withExternal()
                .source(RideState.DRIVER_EN_ROUTE)
                .event(RideEvent.DRIVER_ARRIVED)
                .action(eventProcessor.driverArrived())
                .target(RideState.DRIVER_ARRIVED)


                .and()
                .withExternal().source(RideState.DRIVER_ARRIVED)
                .event(RideEvent.RIDE_STARTED)
                .action(eventProcessor.rideStarted())
                .target(RideState.IN_RIDE)

                .and()
                .withExternal()
                .source(RideState.IN_RIDE)
                .event(RideEvent.RIDE_COMPLETED)
                .action(eventProcessor.rideCompleted())
                .target(RideState.COMPLETED);
    }

    public static <T> T fromContext(StateContext<RideState, RideEvent> context, String key) {
        return (T) context.getExtendedState().getVariables().get(key);
    }

}

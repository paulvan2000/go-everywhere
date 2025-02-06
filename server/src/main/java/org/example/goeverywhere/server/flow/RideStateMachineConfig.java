package org.example.goeverywhere.server.flow;

import org.example.goeverywhere.server.service.DriverService;
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
    private DriverService driverService;

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
                .action(driverService.requestRide())
                .target(RideState.REQUESTED)
                .and()
                // Processing cancellation
                .withExternal()
                .source(RideState.INITIATED)
                .event(RideEvent.NO_AVAILABLE_DRIVERS)
                .action(riderService.noAvailableDrivers())
                .target(RideState.CANCELLED)
                .and()

                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.DRIVER_ACCEPTED)
                .action(driverService.prepareRouteAndSend())
                .target(RideState.DRIVER_EN_ROUTE)


                .and()
                .withExternal()
                .source(RideState.REQUESTED)
                // TODO: add action for rejection
                .event(RideEvent.DRIVER_REJECTED)
                .target(RideState.REQUESTED)
                .and()

                .withExternal()
                .source(RideState.DRIVER_EN_ROUTE)
                .event(RideEvent.DRIVER_ARRIVED)
                .action(driverService.driverArrived())
                .target(RideState.DRIVER_ARRIVED)

                .and()
                .withExternal().source(RideState.DRIVER_ARRIVED).target(RideState.RIDER_ONBOARD).event(RideEvent.RIDER_ONBOARD)
                .and()
                .withExternal().source(RideState.RIDER_ONBOARD).target(RideState.IN_RIDE).event(RideEvent.RIDE_STARTED)
                .and()
                .withExternal().source(RideState.IN_RIDE).target(RideState.COMPLETED).event(RideEvent.RIDE_COMPLETED);
    }

    public static <T> T fromContext(StateContext<RideState, RideEvent> context, String key) {
        return (T) context.getExtendedState().getVariables().get(key);
    }

}

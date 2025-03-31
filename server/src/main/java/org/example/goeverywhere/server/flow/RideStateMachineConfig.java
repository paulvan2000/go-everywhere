// src/main/java/org/example/goeverywhere/server/flow/RideStateMachineConfig.java
package org.example.goeverywhere.server.flow;

import org.example.goeverywhere.server.service.EventProcessor;
// Removed RiderService import as it's not directly used in transitions
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class RideStateMachineConfig extends EnumStateMachineConfigurerAdapter<RideState, RideEvent> {

    @Autowired
    private EventProcessor eventProcessor;

    // Remove @Autowired RiderService if not needed here

    @Override
    public void configure(StateMachineStateConfigurer<RideState, RideEvent> states) throws Exception {
        states
                .withStates()
                .initial(RideState.INITIATED)
                .state(RideState.REQUESTED)
                .state(RideState.DRIVER_EN_ROUTE)
                .state(RideState.DRIVER_ARRIVED)
                .state(RideState.RIDER_ONBOARD) // Added RIDER_ONBOARD if missing
                .state(RideState.IN_RIDE)
                .end(RideState.COMPLETED)
                .end(RideState.CANCELLED);
        // Consider adding a state like BATCH_ASSIGNMENT_PENDING if more complexity is needed
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<RideState, RideEvent> transitions) throws Exception {
        transitions
                // --- Immediate Ride Flow ---
                .withExternal()
                .source(RideState.INITIATED)
                .event(RideEvent.RIDE_REQUESTED) // For immediate requests
                .action(eventProcessor.requestRide())
                .target(RideState.REQUESTED)
                .and()

                // --- Scheduled Ride Flow Trigger ---
                .withExternal()
                .source(RideState.INITIATED)
                .event(RideEvent.SCHEDULED_RIDE_TRIGGERED) // From Batch Scheduler
                .action(eventProcessor.assignBatchedRideToDriver()) // <<< NEW ACTION
                .target(RideState.REQUESTED) // Target state after driver assignment attempt
                .and()

                // --- Common Flow from REQUESTED onwards ---
                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.NO_AVAILABLE_DRIVERS)
                .action(eventProcessor.noAvailableDrivers())
                .target(RideState.CANCELLED)
                .and()

                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.DRIVER_ACCEPTED)
                .action(eventProcessor.driverAccepted())
                .target(RideState.DRIVER_EN_ROUTE)
                .and()

                .withExternal()
                .source(RideState.REQUESTED)
                .event(RideEvent.DRIVER_REJECTED)
                .action(eventProcessor.driverRejected())
                // For batched rides, rejecting might need different logic
                // than restarting the search immediately. For now, back to INITIATED.
                // Consider a CANCELLED state or re-batching logic.
                .target(RideState.INITIATED)
                .and()

                // --- Rest of the transitions (DRIVER_EN_ROUTE -> ARRIVED, etc.) ---
                .withExternal()
                .source(RideState.DRIVER_EN_ROUTE)
                .event(RideEvent.DRIVER_ARRIVED)
                .action(eventProcessor.driverArrived())
                .target(RideState.DRIVER_ARRIVED)
                .and()

                .withExternal()
                .source(RideState.DRIVER_ARRIVED)
                .event(RideEvent.RIDE_STARTED)
                .action(eventProcessor.rideStarted())
                .target(RideState.IN_RIDE) // Assuming IN_RIDE is the correct state
                .and()

                .withExternal()
                .source(RideState.IN_RIDE)
                .event(RideEvent.RIDE_COMPLETED)
                .action(eventProcessor.rideCompleted())
                .target(RideState.COMPLETED);
    }

    // --- Context Helper Methods (Unchanged) ---
    public static <T> T fromContext(StateContext<RideState, RideEvent> context, String key) {
        return (T) context.getExtendedState().getVariables().get(key);
    }
    public static <T> T fromContext(StateMachine<RideState, RideEvent> stateMachine, String key) {
        return (T) stateMachine.getExtendedState().getVariables().get(key);
    }
    public static void toContext(StateMachine<RideState, RideEvent> stateMachine, String key, Object value) {
        stateMachine.getExtendedState().getVariables().put(key, value);
    }
    public static void toContext(StateContext<RideState, RideEvent> context, String key, Object value) {
        context.getExtendedState().getVariables().put(key, value);
    }
}
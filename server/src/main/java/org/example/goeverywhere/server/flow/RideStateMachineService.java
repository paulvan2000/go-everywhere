package org.example.goeverywhere.server.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RideStateMachineService {
    @Autowired
    private StateMachineFactory<RideState, RideEvent> stateMachineFactory;


    // Stores rideId -> state machine mapping
    private final ConcurrentHashMap<String, StateMachine<RideState, RideEvent>> rideMachines = new ConcurrentHashMap<>();


    // Create and store a state machine for a new ride
    public StateMachine<RideState, RideEvent> createStateMachine(String rideId) {
        StateMachine<RideState, RideEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        rideMachines.put(rideId, stateMachine);
        return stateMachine;
    }

    // Retrieve the state machine for a ride
    public StateMachine<RideState, RideEvent> getStateMachine(String rideId) {
        return rideMachines.get(rideId);
    }

    // Trigger a state transition
    public void sendEvent(String rideId, RideEvent event) {
        StateMachine<RideState, RideEvent> stateMachine = getStateMachine(rideId);
        if (stateMachine != null) {
            stateMachine.sendEvent(event);
        }
    }

}

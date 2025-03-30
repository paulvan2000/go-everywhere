package org.example.goeverywhere.server.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RideStateMachineService {

    @Lazy
    @Autowired
    private StateMachineFactory<RideState, RideEvent> stateMachineFactory;


    // Stores riderId -> state machine mapping
    private final ConcurrentHashMap<String, StateMachine<RideState, RideEvent>> rideMachines = new ConcurrentHashMap<>();


    // Create and store a state machine for a new rider
    public StateMachine<RideState, RideEvent> createStateMachine(String riderId) {
        StateMachine<RideState, RideEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        rideMachines.put(riderId, stateMachine);
        return stateMachine;
    }

    // Retrieve the state machine for a ride
    public StateMachine<RideState, RideEvent> getStateMachine(String riderId) {
        return rideMachines.get(riderId);
    }

    // Trigger a state transition
    public void sendEvent(String riderId, RideEvent event) {
        StateMachine<RideState, RideEvent> stateMachine = getStateMachine(riderId);
        if (stateMachine != null) {
            stateMachine.sendEvent(event);
        }
    }

    public void unregisterStateMachine(String rideId) {
        rideMachines.remove(rideId);
    }

    public void stopStateMachine(String riderId) {
        StateMachine<RideState, RideEvent> stateMachine = rideMachines.remove(riderId); // Remove and get
        if (stateMachine != null && !stateMachine.isComplete()) {
            try {
                stateMachine.stopReactively().block(); // Or stateMachine.stop(); if not reactive
                System.out.println("Stopped state machine for riderId: " + riderId);
            } catch (Exception e) {
                System.err.println("Error stopping state machine for riderId " + riderId + ": " + e.getMessage());
            }
        }
    }

}

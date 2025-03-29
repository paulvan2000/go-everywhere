package org.example.goeverywhere.server.flow;

public enum RideState {
    // Start
    INITIATED,
    // The rider has requested a ride.
    REQUESTED,
    // The driver is on the way to pick up the rider
    DRIVER_EN_ROUTE,
    // The driver has arrived at the rider's location
    DRIVER_ARRIVED,
    //  The rider has entered the car.
    RIDER_ONBOARD,
    // The ride is ongoing.
    IN_RIDE,
    // The ride is completed.
    COMPLETED,
    // Cancellation happened
    CANCELLED;
}

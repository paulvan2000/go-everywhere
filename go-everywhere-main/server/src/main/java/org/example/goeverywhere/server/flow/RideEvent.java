package org.example.goeverywhere.server.flow;

public enum RideEvent {
    // Rider submits a ride request.
    RIDE_REQUESTED,
    // A driver is assigned to the ride.
    DRIVER_ASSIGNED,
    // The driver accepts the ride.
    DRIVER_ACCEPTED,
    //  The driver rejects the ride.
    DRIVER_REJECTED,
    // Driver drives to the pick up location.
    DRIVER_EN_ROUTE,
    // The driver arrives at the pickup location.
    DRIVER_ARRIVED,
    // The ride starts.
    RIDE_STARTED,
    //  The ride ends.
    RIDE_COMPLETED,
    // No drivers found to complete the ride
    NO_AVAILABLE_DRIVERS;
}

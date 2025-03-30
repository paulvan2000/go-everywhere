package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.server.flow.RideEvent;
import org.example.goeverywhere.server.flow.RideStateMachineConfig;
import org.example.goeverywhere.server.flow.RideStateMachineService;
import org.example.goeverywhere.server.service.routing.OptimizedRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RideBatchScheduler {

    // Thread-safe list to store pending scheduled rides.
    private final List<RideRequest> pendingRides = new CopyOnWriteArrayList<>();

    @Autowired
    private OptimizedRouteService optimizedRouteService;

    @Autowired
    private RideStateMachineService rideStateMachineService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private UserRegistry userRegistry;

    /**
     * Add a scheduled ride request to the batch.
     */
    public void addRide(RideRequest request) {
        pendingRides.add(request);
    }

    /**
     * Process pending rides every 15 minutes.
     * Adjust the fixed rate as needed.
     */
    @Scheduled(fixedRateString = "900000") // 15 minutes in milliseconds
    public void processBatch() {
        if (pendingRides.isEmpty()) {
            return;
        }
        long now = Instant.now().getEpochSecond();
        long planningWindow = 2 * 3600; // 2 hours

        // Collect rides that are scheduled to occur within the planning window.
        List<RideRequest> ridesToProcess = new ArrayList<>();
        for (RideRequest ride : pendingRides) {
            if (ride.getScheduledPickupTime() > 0 && ride.getScheduledPickupTime() <= now + planningWindow) {
                ridesToProcess.add(ride);
            }
        }
        if (ridesToProcess.isEmpty()) {
            return;
        }
        // Remove processed rides from the pending list.
        pendingRides.removeAll(ridesToProcess);

        // Aggregate pickup and dropoff locations.
        List<LatLng> pickups = new ArrayList<>();
        List<LatLng> dropoffs = new ArrayList<>();
        for (RideRequest ride : ridesToProcess) {
            try {
                LatLng origin = geocodingService.decodeAddress(ride.getOrigin());
                LatLng destination = geocodingService.decodeAddress(ride.getDestination());
                pickups.add(origin);
                dropoffs.add(destination);
            } catch (Exception e) {
                System.err.println("Error decoding addresses: " + e.getMessage());
            }
        }
        // For the depot, choose the first pickup as a simplification.
        LatLng depot = pickups.get(0);

        // Generate the merged route for all rides in this batch.
        Route mergedRoute = optimizedRouteService.generateMergedRoute(depot, pickups, dropoffs);

        // For each ride in the batch, update its state machine with the merged route.
        for (RideRequest ride : ridesToProcess) {
            String riderSessionId = ride.getSessionId();
            var stateMachine = rideStateMachineService.getStateMachine(riderSessionId);
            if (stateMachine == null) {
                stateMachine = rideStateMachineService.createStateMachine(riderSessionId);
            }
            RideStateMachineConfig.toContext(stateMachine, "computedRoute", mergedRoute);
            // Trigger the event that indicates the scheduled ride is now ready.
            stateMachine.sendEvent(RideEvent.SCHEDULED_RIDE_TRIGGERED);
        }
    }
}

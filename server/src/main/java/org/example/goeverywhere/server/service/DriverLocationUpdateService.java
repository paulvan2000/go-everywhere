package org.example.goeverywhere.server.service;

import org.example.goeverywhere.protocol.grpc.DriverEnRoute;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class DriverLocationUpdateService {

    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @Autowired
    private UserRegistry userRegistry;

    // Starts sending periodic location updates
    public void startLocationUpdates(String rideId, String driverSessionId, String riderSessionId) {
        if (runningTasks.containsKey(rideId)) {
            return; // Task is already running
        }

        UserRegistry.Rider rider = userRegistry.getRiderMaybe(riderSessionId).orElseThrow();
        UserRegistry.Driver driver = userRegistry.getDriverMaybe(driverSessionId).orElseThrow();
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            rider.getStreamObserver().onNext(RiderEvent.newBuilder()
                            .setDriverEnRoute(DriverEnRoute.newBuilder().setLocation(driver.location))
                    .build());
        }, 0, 1, TimeUnit.SECONDS);

        runningTasks.put(rideId, task);
        System.out.println("Started location updates for ride " + rideId);
    }

    // Stops the periodic updates
    public void stopLocationUpdates(String rideId) {
        ScheduledFuture<?> task = runningTasks.remove(rideId);
        if (task != null) {
            task.cancel(true);
            System.out.println("Stopped location updates for ride " + rideId);
        }
    }


}

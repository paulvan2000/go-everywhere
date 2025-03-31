package org.example.goeverywhere.server.grpc;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiRiderFlowIntegrationTest extends IntegrationTestBase {

    public static final LatLng driverStartLocation = LatLng.newBuilder().setLatitude(26.382619).setLongitude(-80.204110).build();
    private static String riderSessionId1;
    private static String riderSessionId2;
    private static String driverSessionId;

    private ExecutorService riderExecutor = Executors.newFixedThreadPool(2);
    private ExecutorService driverExecutor = Executors.newSingleThreadExecutor();

    private static WaypointProcessor waypointProcessor;


    @Order(1)
    @Test
    void prepare() {
        signUpDriver();
        signUpRider1();
        signUpRider2();
        driverSessionId = driverLogin().getSessionId();
        riderSessionId1 = riderLogin1().getSessionId();
        riderSessionId2 = riderLogin2().getSessionId();
        waypointProcessor = new WaypointProcessor(userServiceBlockingStub, driverServiceBlockingStub, driverSessionId);
    }


    /**
     * This is an emulation of a full successful flow:
     * - Rider request a ride
     * - Driver accepts
     * - Driver goes to the pickup point while sending location updates along the way
     * - Driver arrives and notifies the system
     * - Driver starts the ride
     * - Driver goes to the destination point while sending location updates along the way
     * - Driver finishes the ride
     */
    @Order(2)
    @Test
    void fullMultiRiderFlow() throws InterruptedException, ExecutionException {
        CountDownLatch driverLatch = new CountDownLatch(1);
        CountDownLatch riderLatch1 = new CountDownLatch(3);
        CountDownLatch riderLatch2 = new CountDownLatch(5);
        Future<?> riderFlowResult1 = riderExecutor.submit(
                riderFlow(driverLatch, new CountDownLatch(0), riderLatch1,
                        riderSessionId1,
                        "9636 Glades Rd, Boca Raton, FL 33434",
                        "8903 Glades Rd Suite H-11, Boca Raton, FL 33434"));
        Future<?> riderFlowResult2 = riderExecutor.submit(
                riderFlow(driverLatch, riderLatch1, riderLatch2,
                        riderSessionId2,
                        "9028 Glades Rd, Boca Raton, FL 33434",
                        "Olympic Heights High School, 20101 Lyons Rd, Boca Raton, FL 33434"));


        Future<?> driverFlowResult = driverExecutor.submit(() -> {

            driverServiceStub.subscribeForRideEvents(SubscribeForRideEventsRequest
                    .newBuilder()
                    .setSessionId(driverSessionId).build(), new TestStreamObserver<>() {
                @Override
                public void onNext(DriverEvent value) {
                    switch (value.getEventCase()) {
                        // a driver gets a notification about the ride
                        case RIDE_REQUESTED -> {

                            RideRequested rideRequested = value.getRideRequested();
                            System.out.println("Driver - Ride requested from riderId=%s, accepting".formatted(rideRequested.getRiderId()));
                            // a driver accepted the ride
                            driverServiceBlockingStub.acceptRide(AcceptRideRequest.newBuilder()
                                    .setRiderId(rideRequested.getRiderId())
                                    .setSessionId(driverSessionId).build());
                        }
                        case RIDE_DETAILS -> {
                            RideDetails rideDetails = value.getRideDetails();
                            // driver starts the ride once the rider gets in
                            System.out.println("Driver - Got a new route");

                            waypointProcessor.setWaypoints(rideDetails.getNewFullRoute().getWaypointsList());
                        }

                        default -> throw new UnsupportedOperationException();
                    }
                }

            });

            userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().setSessionId(driverSessionId).setLocation(driverStartLocation).build());
            driverLatch.countDown();
        });


        riderFlowResult1.get();
        riderFlowResult2.get();
        driverFlowResult.get();
    }

    private static class WaypointProcessor {
        private ScheduledExecutorService driveScheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        private final BlockingDeque<Waypoint> waypointQueue = new LinkedBlockingDeque<>();
        private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
        private final DriverServiceGrpc.DriverServiceBlockingStub driverServiceBlockingStub;

        private final String driverSessionId;


        public WaypointProcessor(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub,
                                 DriverServiceGrpc.DriverServiceBlockingStub driverServiceBlockingStub,
                                 String driverSessionId) {
            this.userServiceBlockingStub = userServiceBlockingStub;
            this.driverServiceBlockingStub = driverServiceBlockingStub;
            this.driverSessionId = driverSessionId;
            driveScheduledExecutor.schedule(() -> processWaypoint(), 100, TimeUnit.MILLISECONDS);
        }

        public void setWaypoints(List<Waypoint> waypoints) {
            waypointQueue.clear();
            waypointQueue.addAll(waypoints);
        }

        private void processWaypoint() {
            try {
                Waypoint waypoint = waypointQueue.takeFirst();
                LatLng location = waypoint.getLocation();
                System.out.println("Driver - On the way, updating coordinates to\n" + location);
                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                        .setSessionId(driverSessionId)
                        .setLocation(location)
                        .build());
                pause();

                // let's check if this waypoint is any special.
                // It can be an origin point, destination point or just a plain intermediary point
                for (WaypointMetadata metadata : waypoint.getWaypointMetadataList()) {
                    switch (metadata.getWaypointType()) {
                        case ORIGIN -> {
                            System.out.println("Driver - Arrived at the pickup point of riderId=" + metadata.getRiderId());
                            driverServiceBlockingStub.driverArrived(DriverArrivedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRiderId(metadata.getRiderId()).build());
                            // a driver picked up a rider
                            System.out.println("Driver - Starting the ride for " + metadata.getRiderId());
                            driverServiceBlockingStub.rideStarted(RideStartedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRiderId(metadata.getRiderId())
                                    .build());
                        }
                        case DESTINATION -> {
                            // driver completes the ride
                            System.out.println("Driver - Arrived at the final destination for riderId = " + metadata.getRiderId());
                            driverServiceBlockingStub.rideCompleted(RideCompletedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRideId(metadata.getRiderId())
                                    .build());
                        }
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // scheduling processing of the next waypoint
                driveScheduledExecutor.schedule(this::processWaypoint, 100, TimeUnit.MILLISECONDS);
            }
        }

    }


    @NotNull
    private Runnable riderFlow(CountDownLatch driverLatch, CountDownLatch previousRiderLatch, CountDownLatch currentRiderLatch, String sessionId, String origin, String destination) {
        return () -> {
            AtomicBoolean driverEnRouteRegistered = new AtomicBoolean(false);

            try {
                driverLatch.await();
                previousRiderLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Rider %s - Requesting a ride".formatted(sessionId));

            // A rider requests a ride
            riderServiceStub.requestRide(RideRequest.newBuilder()
                    .setSessionId(sessionId)
                    .setOrigin(origin)
                    .setDestination(destination)
                    .build(), new TestStreamObserver<>() {
                @Override
                public void onNext(RiderEvent value) {
                    switch (value.getEventCase()) {
                        // a driver accepted the ride
                        case RIDE_ACCEPTED -> {
                            System.out.println("Rider %s - Ride accepted".formatted(sessionId));
                            currentRiderLatch.countDown();
                        }
                        // a driver is on the way
                        case DRIVER_EN_ROUTE -> {
                            System.out.println("Rider %s - Driver is on the way, current coordinates: \n %s ".formatted(sessionId, value.getDriverEnRoute().getLocation()));
                            // there will be multiple events of this time, so we need to count down the latch only once
                            if (driverEnRouteRegistered.compareAndSet(false, true)) {
                                currentRiderLatch.countDown();
                            }
                        }
                        case DRIVER_ARRIVED -> {
                            // a driver is  the pickup point
                            System.out.println("Rider %s - Driver arrived".formatted(sessionId));
                            currentRiderLatch.countDown();
                        }
                        case RIDE_STARTED -> {
                            // a rider gets in the car, and the ride starts
                            System.out.println("Rider %s - Ride started".formatted(sessionId));
                            currentRiderLatch.countDown();
                        }
                        case RIDE_COMPLETED -> {
                            // a rider gets to the destination point
                            System.out.println("Rider %s - Ride completed".formatted(sessionId));
                            currentRiderLatch.countDown();
                        }


                    }
                }
            });

            try {
                currentRiderLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }


    private abstract static class TestStreamObserver<T> implements StreamObserver<T> {
        @Override
        public void onError(Throwable t) {
            System.out.println("Error: " + t.getMessage());
        }

        @Override
        public void onCompleted() {
            System.out.println("Observer completed");
        }
    }

}

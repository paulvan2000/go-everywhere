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
    private static String driverSessionId;
    private static String riderSessionId1;
    private static String riderSessionId2;

    private ExecutorService riderExecutor = Executors.newFixedThreadPool(2);
    private ExecutorService driverExecutor = Executors.newSingleThreadExecutor();


    @Order(1)
    @Test
    void prepare() {
        signUpDriver();
        signUpRider1();
        signUpRider2();
        driverSessionId = driverLogin().getSessionId();
        riderSessionId1 = riderLogin1().getSessionId();
        riderSessionId2 = riderLogin2().getSessionId();
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
//    @Order(2)
//    @Test
//    void fullMultiRiderFlow() throws InterruptedException, ExecutionException {
//        CountDownLatch driverLatch = new CountDownLatch(1);
//        CountDownLatch riderLatch1 = new CountDownLatch(3);
//        CountDownLatch riderLatch2 = new CountDownLatch(5);
//        Future<?> riderFlowResult1 = riderExecutor.submit(
//                riderFlow(driverLatch, new CountDownLatch(0), riderLatch1,
//                1,
//                "9636 Glades Rd, Boca Raton, FL 33434",
//                "8903 Glades Rd Suite H-11, Boca Raton, FL 33434"));
//        Future<?> riderFlowResult2 = riderExecutor.submit(
//                riderFlow(driverLatch, riderLatch1, riderLatch2,
//                2,
//                "9028 Glades Rd, Boca Raton, FL 33434",
//                "Olympic Heights High School, 20101 Lyons Rd, Boca Raton, FL 33434"));
//
//
//        Future<?> driverFlowResult = driverExecutor.submit(() -> {
//
//            driverServiceStub.subscribeForRideEvents(SubscribeForRideEventsRequest
//                    .newBuilder()
//                    .setSessionId(driverSessionId).build(), new TestStreamObserver<>() {
//                @Override
//                public void onNext(DriverEvent value) {
//                    switch (value.getEventCase()) {
//                        // a driver gets a notification about the ride
//                        case RIDE_REQUESTED:
//                            System.out.println("Driver - Ride requested, accepting");
//                            RideRequested rideRequested = value.getRideRequested();
//
//                            // a driver accepted the ride
//                            driverServiceBlockingStub.acceptRide(AcceptRideRequest.newBuilder()
//                                    .setRiderId(rideRequested.getRiderId())
//                                    .setSessionId(driverSessionId).build());
//                            return;
//
//                        case RIDE_ACCEPTED:
//                            System.out.println("Driver - Got the route to the pickup point");
//                            RideAccepted rideAccepted = value.getRideAccepted();
//                            Route route = rideAccepted.getRouteToRider();
//                            // emulating moving to the pickup location
//                            for (Waypoint waypoint : route.getWaypointsList()) {
//                                LatLng location = waypoint.getLocation();
//                                System.out.println("Driver - On the way, updating coordinates to " + location);
//                                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
//                                        .setSessionId(driverSessionId)
//                                        .setLocation(location)
//                                        .build());
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                            driverServiceBlockingStub.driverArrived(DriverArrivedRequest.newBuilder()
//                                    .setSessionId(driverSessionId)
//                                    .setRiderId(riderSessionId1).build());
//                            driverServiceBlockingStub.rideStarted(RideStartedRequest.newBuilder()
//                                    .setSessionId(driverSessionId)
//                                    .setRiderId(riderSessionId1)
//                                    .build());
//                            return;
//                        case RIDE_DETAILS:
//                            System.out.println("Driver - Got a ride details");
//                            RideDetails rideDetails = value.getRideDetails();
//                            // driver starts the ride once the rider gets in
//                            List<Waypoint> waypointsList = rideDetails.getRouteToDestination().getWaypointsList();
//                            // emulating moving to the destination
//                            for (Waypoint waypoint : waypointsList) {
//                                LatLng location = waypoint.getLocation();
//                                System.out.println("Driver - On the way, updating coordinates to " + location);
//                                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
//                                        .setSessionId(driverSessionId)
//                                        .setLocation(location)
//                                        .build());
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                            // driver completes the ride
//                            driverServiceBlockingStub.rideCompleted(RideCompletedRequest.newBuilder()
//                                    .setSessionId(driverSessionId)
//                                    .setRideId(riderSessionId1)
//                                    .build());
//                        default:
//                            System.out.println("Driver - Default");
//
//                    }
//                }
//
//            });
//
//            userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().setSessionId(driverSessionId).setLocation(driverStartLocation).build());
//            driverLatch.countDown();
//        });
//
//
//        riderFlowResult1.get();
//        riderFlowResult2.get();
//        driverFlowResult.get();
//    }

    @NotNull
    private Runnable riderFlow(CountDownLatch driverLatch,  CountDownLatch previousRiderLatch,  CountDownLatch currentRiderLatch, int id, String origin, String destination) {
        return () -> {
            AtomicBoolean driverEnRouteRegistered = new AtomicBoolean(false);

            try {
                driverLatch.await();
                previousRiderLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Rider %d - Requesting a ride".formatted(id));

            // A rider requests a ride
            riderServiceStub.requestRide(RideRequest.newBuilder()
                    .setSessionId(riderSessionId1)
                    .setOrigin(origin)
                    .setDestination(destination)
                    .build(), new TestStreamObserver<>() {
                @Override
                public void onNext(RiderEvent value) {
                    switch (value.getEventCase()) {
                        // a driver accepted the ride
                        case RIDE_ACCEPTED:
                            System.out.println("Rider %d - Ride accepted".formatted(id));
                            currentRiderLatch.countDown();
                            return;
                        // a driver is on the way
                        case DRIVER_EN_ROUTE:
                            System.out.println("Rider %d - Driver is on the way, current coordinates: %s ".formatted(id, value.getDriverEnRoute().getLocation()));
                            // there will be multiple events of this time, so we need to count down the latch only once
                            if (driverEnRouteRegistered.compareAndSet(false, true)) {
                                currentRiderLatch.countDown();
                            }
                            return;
                        case DRIVER_ARRIVED:
                            // a driver is  the pickup point
                            System.out.println("Rider %d - Driver arrived".formatted(id));
                            currentRiderLatch.countDown();
                            return;
                        case RIDE_STARTED:
                            // a rider gets in the car, and the ride starts
                            System.out.println("Rider %d - Ride started".formatted(id));
                            currentRiderLatch.countDown();
                            return;
                        case RIDE_COMPLETED:
                            // a rider gets to the destination point
                            System.out.println("Rider %d - Ride completed".formatted(id));
                            currentRiderLatch.countDown();

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


    /**
     * This test scenario driver reject the rider but it's the only available driver so the system cancels the ride
     */
    @Order(3)
    @Test
    void riderFlow_theOnlyDriverRejects() throws InterruptedException, ExecutionException {
        CountDownLatch driverLatch = new CountDownLatch(1);
        CountDownLatch riderLatch = new CountDownLatch(2);
        Future<?> riderFlowResult = riderExecutor.submit(() -> {
            System.out.println("Rider - Requesting a ride");
            try {
                driverLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // A rider requests a ride
            riderServiceStub.requestRide(RideRequest.newBuilder()
                    .setSessionId(riderSessionId1)
                    .setOrigin("9636 Glades Rd, Boca Raton, FL 33434")
                    .setDestination("8903 Glades Rd Suite H-11, Boca Raton, FL 33434")
                    .build(), new TestStreamObserver<>() {
                @Override
                public void onNext(RiderEvent value) {
                    switch (value.getEventCase()) {
                        // a driver rejects it
                        case DRIVER_REJECTED:
                            System.out.println("Rider - Driver rejected");
                            riderLatch.countDown();
                            return;
                        // the system sees no other way but to cancel the ride as there is no one to execute it
                        case SYSTEM_CANCELLED:
                            System.out.println("Rider - System cancelled the ride. Message: " + value.getSystemCancelled().getMessage());
                            riderLatch.countDown();


                    }
                }
            });

            try {
                riderLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        Future<?> driverFlowResult = driverExecutor.submit(() -> {

            driverServiceStub.subscribeForRideEvents(SubscribeForRideEventsRequest
                    .newBuilder()
                    .setSessionId(driverSessionId).build(), new TestStreamObserver<>() {
                @Override
                public void onNext(DriverEvent value) {
                    switch (value.getEventCase()) {
                        case RIDE_REQUESTED:
                            System.out.println("Driver - Ride requested, rejection");
                            RideRequested rideRequested = value.getRideRequested();
                            driverServiceBlockingStub.rejectRide(RejectRideRequest.newBuilder()
                                    .setRiderId(riderSessionId1)
                                    .setSessionId(driverSessionId).build());
                    }
                }
            });

            userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().setSessionId(driverSessionId).setLocation(driverStartLocation).build());
            driverLatch.countDown();
        });


        riderFlowResult.get();
        driverFlowResult.get();
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

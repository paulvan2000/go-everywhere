package org.example.goeverywhere.server.grpc;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.service.routing.MockRouteService;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderFlowIntegrationTest extends IntegrationTestBase {

    public static final LatLng riderStartLocation = LatLng.newBuilder().setLatitude(26.367268).setLongitude(-80.197098).build();
    public static final LatLng driverStartLocation = LatLng.newBuilder().setLatitude(26.382619).setLongitude(-80.204110).build();
    private static String driverSessionId;
    private static String riderSessionId;

    private ExecutorService riderExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService driverExecutor = Executors.newSingleThreadExecutor();


    @Order(1)
    @Test
    void prepare() {
        signUpDriver();
        signUpRider();
        driverSessionId = driverLogin().getSessionId();
        riderSessionId = riderLogin().getSessionId();
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
    void fullRiderFlow() throws InterruptedException, ExecutionException {
        CountDownLatch driverLatch = new CountDownLatch(1);
        CountDownLatch riderLatch = new CountDownLatch(5);
        Future<?> riderFlowResult = riderExecutor.submit(() -> {
            AtomicBoolean driverEnRouteRegistered = new AtomicBoolean(false);

            System.out.println("Rider - Requesting a ride");
            try {
                driverLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // A rider requests a ride
            riderServiceStub.requestRide(RideRequest.newBuilder()
                    .setSessionId(riderSessionId)
                    .setOrigin("9636 Glades Rd, Boca Raton, FL 33434")
                    .setDestination("8903 Glades Rd Suite H-11, Boca Raton, FL 33434")
                    .build(), new TestStreamObserver<>() {
                @Override
                public void onNext(RiderEvent value) {
                    switch (value.getEventCase()) {
                        // a driver accepted the ride
                        case RIDE_ACCEPTED:
                            System.out.println("Rider - Ride accepted");
                            riderLatch.countDown();
                            return;
                        // a driver is on the way
                        case DRIVER_EN_ROUTE:
                            System.out.println("Rider - Driver is on the way, current coordinates: " + value.getDriverEnRoute().getLocation());
                            // there will be multiple events of this time, so we need to count down the latch only once
                            if(driverEnRouteRegistered.compareAndSet(false, true)){
                                riderLatch.countDown();
                            }
                            return;
                        case DRIVER_ARRIVED:
                            // a driver is  the pickup point
                            System.out.println("Rider - Driver arrived");
                            riderLatch.countDown();
                            return;
                        case RIDE_STARTED:
                            // a rider gets in the car, and the ride starts
                            System.out.println("Rider - Ride started");
                            riderLatch.countDown();
                            return;
                         case RIDE_COMPLETED:
                             // a rider gets to the destination point
                             System.out.println("Rider - Ride completed");
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
                        // a driver gets a notification about the ride
                        case RIDE_REQUESTED:
                            System.out.println("Driver - Ride requested, accepting");
                            RideRequested rideRequested = value.getRideRequested();

                            // a driver accepted the ride
                            driverServiceBlockingStub.acceptRide(AcceptRideRequest.newBuilder()
                                    .setRideId(rideRequested.getRideId())
                                    .setSessionId(driverSessionId).build());
                            return;

                        case RIDE_ACCEPTED:
                            System.out.println("Driver - Got the route to the pickup point");
                            RideAccepted rideAccepted = value.getRideAccepted();
                            Route route = rideAccepted.getRouteToRider();
                            // emulating moving to the pickup location
                            for (Waypoint waypoint : route.getWaypointsList()) {
                                LatLng location = waypoint.getLocation();
                                System.out.println("Driver - On the way, updating coordinates to " + location);
                                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                                        .setSessionId(driverSessionId)
                                        .setLocation(location)
                                        .build());
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            driverServiceBlockingStub.driverArrived(DriverArrivedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRideId(rideAccepted.getRideId()).build());
                            return;
                        case RIDE_DETAILS:
                            System.out.println("Driver - Got a ride details");
                            RideDetails rideDetails = value.getRideDetails();
                            // driver starts the ride once the rider gets in
                            driverServiceBlockingStub.rideStarted(RideStartedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRideId(rideDetails.getRideId())
                                    .build());
                            List<Waypoint> waypointsList = rideDetails.getRouteToDestination().getWaypointsList();
                            // emulating moving to the destination
                            for (Waypoint waypoint : waypointsList) {
                                LatLng location = waypoint.getLocation();
                                System.out.println("Driver - On the way, updating coordinates to " + location);
                                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                                        .setSessionId(driverSessionId)
                                        .setLocation(location)
                                        .build());
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // driver completes the ride
                            driverServiceBlockingStub.rideCompleted(RideCompletedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRideId(rideDetails.getRideId())
                                    .build());

                    }
                }

            });

            userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().setSessionId(driverSessionId).setLocation(driverStartLocation).build());
            driverLatch.countDown();
        });


        riderFlowResult.get();
        driverFlowResult.get();
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
                    .setSessionId(riderSessionId)
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
                                    .setRideId(rideRequested.getRideId())
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

    private abstract class TestStreamObserver<T> implements StreamObserver<T> {
        @Override
        public void onError(Throwable t) {
            // no op
        }

        @Override
        public void onCompleted() {
            // no op
        }
    }

}

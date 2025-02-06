package org.example.goeverywhere.server.grpc;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.*;
import org.example.goeverywhere.server.service.routing.MockRouteService;
import org.junit.jupiter.api.*;

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

    @Order(2)
    @Test
    void riderFlow() throws InterruptedException, ExecutionException {
        CountDownLatch driverLatch = new CountDownLatch(1);
        CountDownLatch riderLatch = new CountDownLatch(3);
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
                    .build(), new StreamObserver<>() {
                @Override
                public void onNext(RiderEvent value) {
                    switch (value.getEventCase()) {
                        // a driver accepted the ride
                        case RIDE_ACCEPTED:
                            System.out.println("Rider - Ride accepted");
                            riderLatch.countDown();
                            return;
                        case DRIVER_EN_ROUTE:
                            System.out.println("Rider - Driver is on the way, current coordinates: " + value.getDriverEnRoute().getLocation());
                            if(driverEnRouteRegistered.compareAndSet(false, true)){
                                riderLatch.countDown();
                            }
                            return;
                        case DRIVER_ARRIVED:
                            System.out.println("Rider - Driver arrived");
                            riderLatch.countDown();
                    }
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });

            try {
                riderLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        Future<?> driverFlowResult = driverExecutor.submit(() -> {

            driverServiceStub.subscribeForRideEvents(SubscribeForRideEvents
                    .newBuilder()
                    .setSessionId(driverSessionId).build(), new StreamObserver<>() {
                @Override
                public void onNext(DriverEvent value) {
                    switch (value.getEventCase()) {
                        case RIDE_REQUESTED:
                            System.out.println("Driver - Ride requested, accepting");
                            RideRequested rideRequested = value.getRideRequested();
                            driverServiceBlockingStub.acceptRide(AcceptRideRequest.newBuilder()
                                    .setRideId(rideRequested.getRideId())
                                    .setSessionId(driverSessionId).build());

                            Route route = MockRouteService.generateMockRoute(driverStartLocation, rideRequested.getPickupLocation());
                            for (Waypoint waypoint : route.getWaypointsList()) {
                                System.out.println("Driver - On the way, updating coordinates");
                                userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                                        .setSessionId(driverSessionId)
                                        .setLocation(waypoint.getLocation())
                                        .build());
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            driverServiceBlockingStub.driverArrived(DriverArrivedRequest.newBuilder()
                                    .setSessionId(driverSessionId)
                                    .setRideId(rideRequested.getRideId()).build());
                            System.out.println("Driver - Arrived");
                    }
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });

            userServiceBlockingStub.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().setSessionId(driverSessionId).setLocation(driverStartLocation).build());
            driverLatch.countDown();
        });


        riderFlowResult.get();
        driverFlowResult.get();
    }

}

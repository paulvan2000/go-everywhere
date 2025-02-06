package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.DriverEvent;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.example.goeverywhere.server.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UserRegistry {

    @Autowired
    SessionStore sessionStore;

    // Radius of Earth in km
    public static final double EARTH_RADIUS = 6371;
    public static final int METERS_IN_KILOMETER = 1000;
    private ConcurrentHashMap<String, Driver> drivers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Rider> riders  = new ConcurrentHashMap<>();

    public void registerRider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if(userType != UserType.RIDER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a RIDER");
        }
        riders.put(sessionId, new Rider(sessionId, streamObserver));
    }

    public void registerDriver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
        UserType userType = sessionStore.getUserType(sessionId);
        if(userType != UserType.DRIVER) {
            throw new IllegalArgumentException("Session " + sessionId + " is not for a DRIVER");
        }

        drivers.put(sessionId, new Driver(sessionId, streamObserver));
    }

    public void updateUserLocation(String sessionId, LatLng location) {
        UserType userType = sessionStore.getUserType(sessionId);
        switch (userType) {
            case RIDER -> {
                Rider rider = riders.get(sessionId);
                if(rider == null) {
                    throw new IllegalArgumentException("Unknown sessionId " + sessionId);
                }
                rider.location = location;
            }
            case DRIVER -> {
                Driver driver = drivers.get(sessionId);
                if(driver == null) {
                    throw new IllegalArgumentException("Unknown sessionId " + sessionId);
                }
                driver.location = location;
            }
        }

    }

    public Optional<Driver> getDriverMaybe(String sessionId) {
        return Optional.ofNullable(drivers.get(sessionId));
    }
    public Optional<Rider> getRiderMaybe(String sessionId) {
        return Optional.ofNullable(riders.get(sessionId));
    }

    public void updateDriverAvailability(String sessionId, boolean available) {
        Driver driver = drivers.get(sessionId);
        if(driver == null) {
            throw new IllegalArgumentException("Driver with sessionId=%s not found".formatted(sessionId));
        }
        driver.available.set(available);
    }


    /**
     * Returns a closest driver to the provided location
     * @param location location to compare with
     * @return empty if no drivers are in the pool, the closest driver otherwise
     */
    public Optional<Driver> findClosestAvailableDriver(LatLng location, String rideId) {
        return drivers.entrySet().parallelStream()
                .filter(v -> {
                    Driver driver = v.getValue();
                    return driver.location != null && driver.available.get() && !driver.rejectedRides.contains(rideId);
                })
                .sorted((kv1, kv2) ->
                        (int) (RouteService.calculateDistance(kv1.getValue().location, location) - RouteService.calculateDistance(kv2.getValue().location, location))
                )
                .map(Map.Entry::getValue)
                .findFirst();
    }


    private abstract static class AbstractUser<T> {

        private final String sessionId;
        private final StreamObserver<T> streamObserver;

        volatile LatLng location;

        public AbstractUser(String sessionId, StreamObserver<T> streamObserver) {
            this.sessionId = sessionId;
            this.streamObserver = streamObserver;
        }

        public String getSessionId() {
            return sessionId;
        }

        public StreamObserver<T> getStreamObserver() {
            return streamObserver;
        }
    }

    public static class Driver extends AbstractUser<DriverEvent> {
        private AtomicBoolean available = new AtomicBoolean(true);
        private Set<String> rejectedRides = ConcurrentHashMap.newKeySet();

        public Driver(String sessionId, StreamObserver<DriverEvent> streamObserver) {
            super(sessionId, streamObserver);
        }
    }

    public static class Rider extends AbstractUser<RiderEvent> {
        public Rider(String sessionId, StreamObserver<RiderEvent> streamObserver) {
            super(sessionId, streamObserver);
        }
    }

}

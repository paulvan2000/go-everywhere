package org.example.goeverywhere.server.service;

import com.google.type.LatLng;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.example.goeverywhere.server.data.model.User;
import org.example.goeverywhere.server.service.routing.RouteService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.openMocks;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRegistryTest {

    private static String driverId1;
    private static String driverId2;

    private static String riderId1;

    private static String ride1;
    private static LatLng latLng1;
    private static LatLng latLng2;
    private static LatLng riderOrigin;
    private static LatLng riderDestination;
    private static Route route;


    private UserRegistry userRegistry;

    @Mock
    private RouteService routeService;


    @BeforeAll
    public static void setUp() {
        riderId1 = "rider1";
        driverId1 = "driver1";
        driverId2 = "driver2";
        ride1 = "ride1";
        latLng1 = LatLng.newBuilder().setLongitude(37.5611866).setLatitude(-122.3212332).build(); //  San Mateo Japanese garden
        latLng2 = LatLng.newBuilder().setLongitude(37.426919).setLatitude(-122.1728289).build(); //Stanford
        riderOrigin = LatLng.newBuilder().setLongitude(37.434321).setLatitude(-122.160768).build(); // Stanford Stadium
        riderDestination  =  LatLng.newBuilder().setLongitude(37.4437976).setLatitude(-122.1647358).build();
        route = Route.newBuilder()
                .addWaypoints(Waypoint.newBuilder().setLocation(riderOrigin))
                .addWaypoints(Waypoint.newBuilder().setLocation(riderDestination))
                .build();

    }

    @BeforeEach
    public void beforeEach() {
        openMocks(this);
        userRegistry = new UserRegistry();
        var sessionStore = new SessionStore();
        userRegistry.sessionStore = sessionStore;
        userRegistry.routeService = routeService;
        User rider1 = new User();
        rider1.setUserType(UserType.RIDER);
        User driver1 = new User();
        driver1.setUserType(UserType.DRIVER);
        User driver2 = new User();
        driver2.setUserType(UserType.DRIVER);
        sessionStore.registerSession(riderId1, rider1);
        sessionStore.registerSession(driverId1, driver1);
        sessionStore.registerSession(driverId2, driver2);

    }

    @Test
    public void initState() {
        // driver pool is empty initially
        assertTrue(userRegistry.getRiderMaybe(riderId1).isEmpty());
        assertTrue(userRegistry.getDriverMaybe(driverId1).isEmpty());
        assertTrue(userRegistry.getDriverMaybe(driverId2).isEmpty());
        assertTrue(userRegistry.findAvailableDriverAndNewRoute(route, ride1).isEmpty());

        // these operations are illegal on empty registry or for unknown sessionId
        assertThrows(IllegalArgumentException.class, () -> userRegistry.updateUserLocation(driverId2, latLng1));
        assertThrows(IllegalArgumentException.class, () -> userRegistry.updateUserLocation(riderId1, riderOrigin));
    }

    @Test
    public void testAdd() {
        userRegistry.registerDriver(driverId1, mock(StreamObserver.class));
        userRegistry.registerDriver(driverId2, mock(StreamObserver.class));
        userRegistry.registerRider(riderId1, mock(StreamObserver.class));


        assertTrue(userRegistry.getDriverMaybe(driverId1).isPresent());
        assertTrue(userRegistry.getDriverMaybe(driverId2).isPresent());
        assertTrue(userRegistry.getRiderMaybe(riderId1).isPresent());
    }

    @Test
    public void testUpdateUserLocation() {
        // register drivers
        testAdd();

        // drivers have no locations, so no results found
        assertTrue(userRegistry.findAvailableDriverAndNewRoute(route, ride1).isEmpty());
        // registering a driver
        userRegistry.updateUserLocation(driverId1, latLng1);
        //assertEquals(latLng1, userRegistry.findAvailableDriverAndNewRoute(route, ride1).get().location);
        // registering a driver that is closer
        userRegistry.updateUserLocation(driverId2, latLng2);
        //assertEquals(latLng2, userRegistry.findAvailableDriverAndNewRoute(route, ride1).get().location);
    }

    @Test
    public void testUpdateDriverAvailability() {
        // register drivers
//        testAdd();
//        userRegistry.updateUserLocation(driverId1, latLng1);
//        userRegistry.updateUserLocation(driverId2, latLng2);
//        assertEquals(latLng2, userRegistry.findAvailableDriverAndNewRoute(riderOrigin, ride1).get().location);
//
//        // the closest driver is busy now
//        userRegistry.reserveDriverForRide(driverId2);
//
//        // getting the one that is available
//        assertEquals(latLng1, userRegistry.findClosestAvailableIdleDriver(riderOrigin, ride1).get().location);
//
//        // the closest driver is available now
//        userRegistry.releaseDriverFromRide(driverId2);
//
//        assertEquals(latLng2, userRegistry.findClosestAvailableIdleDriver(riderOrigin, ride1).get().location);

    }

    @Test
    public void testUpdateRiderLocation() {
        // register drivers
        testAdd();
        userRegistry.updateUserLocation(riderId1, riderOrigin);

        assertEquals(riderOrigin, userRegistry.getRiderMaybe(riderId1).get().location);
    }


}
package org.example.goeverywhere.server.service.routing;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.example.goeverywhere.protocol.grpc.WaypointMetadata;
import org.example.goeverywhere.protocol.grpc.WaypointType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.goeverywhere.server.service.routing.RouteService.calculateDistance;

@Service("mockRouteService")
public class MockRouteService implements RouteService {

    @Autowired
    private GoogleMapsRouteService googleMapsRouteService;

    @Override
    public Route generateRoute(LatLng start, LatLng destination, String riderSessionId) {
        return generateMockRoute(start, destination, riderSessionId);
    }

    @NotNull
    public static Route generateMockRoute(LatLng start, LatLng destination, String riderSessionId) {
        int numWaypoints = 5; // Define the number of intermediate points
        List<Waypoint> waypoints = new ArrayList<>();

        for (int i = 0; i <= numWaypoints; i++) {
            double fraction = (double) i / numWaypoints;
            double lat = start.getLatitude() + (destination.getLatitude() - start.getLatitude()) * fraction;
            double lon = start.getLongitude() + (destination.getLongitude() - start.getLongitude()) * fraction;

            Waypoint.Builder waypoint = Waypoint.newBuilder()
                    .setLocation(LatLng.newBuilder().setLatitude(lat).setLongitude(lon).build())
                    .setDistanceFromStartKm(fraction * calculateDistance(start, destination))
                    .setDurationFromStartMin(fraction * 15); // Assume total ride time is 15 min

            if(riderSessionId != null) {
                if(waypoint.getLocation().equals(start)) {
                    waypoint.addWaypointMetadata(WaypointMetadata.newBuilder().setWaypointType(WaypointType.ORIGIN).setRiderId(riderSessionId));
                }

                if(waypoint.getLocation().equals(destination)) {
                    waypoint.addWaypointMetadata(WaypointMetadata.newBuilder().setWaypointType(WaypointType.DESTINATION).setRiderId(riderSessionId));
                }
            }

            waypoints.add(waypoint.build());
        }

        return Route.newBuilder()
                .addAllWaypoints(waypoints)
                .setTotalDistanceKm(calculateDistance(start, destination))
                .setTotalDurationMin(15)
                .build();
    }

    @Override
    public Optional<Route> tryMergeRoutes(Route existingRoute, Route newPassengerRoute) {
        return googleMapsRouteService.tryMergeRoutes(existingRoute, newPassengerRoute);
    }

    @Override
    public Route getRouteSegment(Route route, LatLng start, LatLng destination) {
        return googleMapsRouteService.getRouteSegment(route, start, destination);
    }

}

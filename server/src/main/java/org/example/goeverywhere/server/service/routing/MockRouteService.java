package org.example.goeverywhere.server.service.routing;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.goeverywhere.server.service.routing.RouteService.calculateDistance;

@Service("mockRouteService")
public class MockRouteService implements RouteService {
    @Override
    public Route generateRoute(LatLng start, LatLng destination) {
        return generateMockRoute(start, destination);
    }

    @NotNull
    public static Route generateMockRoute(LatLng start, LatLng destination) {
        int numWaypoints = 5; // Define the number of intermediate points
        List<Waypoint> waypoints = new ArrayList<>();

        for (int i = 0; i <= numWaypoints; i++) {
            double fraction = (double) i / numWaypoints;
            double lat = start.getLatitude() + (destination.getLatitude() - start.getLatitude()) * fraction;
            double lon = start.getLongitude() + (destination.getLongitude() - start.getLongitude()) * fraction;

            Waypoint waypoint = Waypoint.newBuilder()
                    .setLocation(LatLng.newBuilder().setLatitude(lat).setLongitude(lon).build())
                    .setDistanceFromStartKm(fraction * calculateDistance(start, destination))
                    .setDurationFromStartMin(fraction * 15) // Assume total ride time is 15 min
                    .build();

            waypoints.add(waypoint);
        }

        return Route.newBuilder()
                .addAllWaypoints(waypoints)
                .setTotalDistanceKm(calculateDistance(start, destination))
                .setTotalDurationMin(15)
                .build();
    }

}

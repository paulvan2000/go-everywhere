package org.example.goeverywhere.server.service.routing;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;

import static org.example.goeverywhere.server.service.routing.RouteService.calculateDistance;

public class RouteUtils {

    /**
     * Generates a route with exactly three waypoints:
     *  1) The origin
     *  2) A midpoint between origin and destination
     *  3) The destination
     *
     * Distances and durations are computed for demonstration, assuming a constant speed.
     * Feel free to replace or refine the speed/time logic as needed.
     */
    public static Route generate3PointRoute(LatLng origin, LatLng destination) {
        // 1. Compute total distance in meters
        double totalDistanceMeters = calculateDistance(origin, destination);

        // 2. Assume a constant speed of 60 km/h => 1 km/min => totalDist(km) ~ totalDuration(min)
        double totalDistanceKm = totalDistanceMeters / 1000.0;
        double totalDurationMin = totalDistanceKm; // e.g., 1 km => 1 min, if speed = 60km/h

        // 3. Calculate the midpoint
        LatLng midpoint = computeMidpoint(origin, destination);

        // 4. Distances for origin->midpoint and midpoint->destination
        double distOriginToMid = calculateDistance(origin, midpoint) / 1000.0;       // km
        double distMidToDest = totalDistanceKm - distOriginToMid;                   // km

        // Duration assumptions
        double durOriginToMid = distOriginToMid; // in minutes
        double durMidToDest   = distMidToDest;   // in minutes

        // 5. Build three waypoints
        Waypoint wpOrigin = Waypoint.newBuilder()
                .setLocation(origin)
                .setDistanceFromStartKm(0.0)
                .setDurationFromStartMin(0.0)
                .build();

        Waypoint wpMidpoint = Waypoint.newBuilder()
                .setLocation(midpoint)
                .setDistanceFromStartKm(distOriginToMid)
                .setDurationFromStartMin(durOriginToMid)
                .build();

        Waypoint wpDestination = Waypoint.newBuilder()
                .setLocation(destination)
                .setDistanceFromStartKm(totalDistanceKm)
                .setDurationFromStartMin(totalDurationMin)
                .build();

        // 6. Assemble the final route
        return Route.newBuilder()
                .addWaypoints(wpOrigin)
                .addWaypoints(wpMidpoint)
                .addWaypoints(wpDestination)
                .setTotalDistanceKm(totalDistanceKm)
                .setTotalDurationMin(totalDurationMin)
                .build();
    }

    /**
     * Computes the geographic midpoint between two LatLng points
     * using a simple spherical approximation.
     */
    private static LatLng computeMidpoint(LatLng origin, LatLng destination) {
        double lat1 = Math.toRadians(origin.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon2 = Math.toRadians(destination.getLongitude());

        double bx = Math.cos(lat2) * Math.cos(lon2 - lon1);
        double by = Math.cos(lat2) * Math.sin(lon2 - lon1);

        double lat3 = Math.atan2(
                Math.sin(lat1) + Math.sin(lat2),
                Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by)
        );
        double lon3 = lon1 + Math.atan2(by, Math.cos(lat1) + bx);

        // Convert back to degrees
        lat3 = Math.toDegrees(lat3);
        lon3 = Math.toDegrees(lon3);

        return LatLng.newBuilder().setLatitude(lat3).setLongitude(lon3).build();
    }

}

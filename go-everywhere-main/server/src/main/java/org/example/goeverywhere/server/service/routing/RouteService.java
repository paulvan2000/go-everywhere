package org.example.goeverywhere.server.service.routing;

import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.server.service.UserRegistry;

import java.util.Optional;

public interface RouteService {
    /**
     * Calculate distance between two points using Haversine formula (for Earth’s curvature)
     * @param latLng1 lat/long of the first point
     * @param latLng2 lat/long of the second point
     * @return the distance in meters
     */
    static double calculateDistance(LatLng latLng1, LatLng latLng2) {
        double dLat = Math.toRadians(latLng2.getLatitude() - latLng1.getLatitude());
        double dLon = Math.toRadians(latLng2.getLongitude() - latLng1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latLng2.getLatitude())) * Math.cos(Math.toRadians(latLng1.getLatitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return UserRegistry.EARTH_RADIUS * c * UserRegistry.METERS_IN_KILOMETER;
    }

    Route generateRoute(LatLng start, LatLng destination, String riderSessionId);

    Optional<Route> tryMergeRoutes(Route existingRoute, Route newPassengerRoute);

    Route getRouteSegment(Route route, LatLng start, LatLng destination);
}
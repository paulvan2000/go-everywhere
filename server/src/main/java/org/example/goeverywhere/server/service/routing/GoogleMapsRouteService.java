package org.example.goeverywhere.server.service.routing;

import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("googleRouteService")
public class GoogleMapsRouteService implements RouteService {

    private final GeoApiContext geoApiContext;

    public GoogleMapsRouteService() {
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey("YOUR_GOOGLE_MAPS_API_KEY")
                .build();
    }

    @Override
    public Route generateRoute(LatLng start, LatLng destination) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(start.getLatitude() + "," + start.getLongitude())
                    .destination(destination.getLatitude() + "," + destination.getLongitude())
                    .await();

            DirectionsRoute route = result.routes[0];

            Route.Builder routeBuilder = Route.newBuilder()
                    .setTotalDistanceKm(route.legs[0].distance.inMeters / 1000.0)
                    .setTotalDurationMin(route.legs[0].duration.inSeconds / 60.0);

            Arrays.stream(route.legs[0].steps).forEach(step -> routeBuilder.addWaypoints(
                    Waypoint.newBuilder()
                            .setLocation(LatLng.newBuilder()
                                    .setLatitude(step.startLocation.lat)
                                    .setLongitude(step.startLocation.lng)
                                    .build())
                            .setDistanceFromStartKm(step.distance.inMeters / 1000.0)
                            .setDurationFromStartMin(step.duration.inSeconds / 60.0)
                            .build()));

            return routeBuilder.build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching route from Google Maps", e);
        }
    }
}
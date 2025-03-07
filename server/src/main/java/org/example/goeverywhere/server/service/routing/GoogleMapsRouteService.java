package org.example.goeverywhere.server.service.routing;

import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("googleRouteService")
public class GoogleMapsRouteService implements RouteService {

    private static final double ALLOWED_INCREASE_RATIO = 1.30; // +30%

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

    /**
     * This method calculates a segment of the full route that is defined by the origin and the destination
     * @param fullRoute full route
     * @param origin origin point
     * @param destination destination point
     * @return route segment
     */
    @Override
    public Route getRouteSegment(Route fullRoute, LatLng origin, LatLng destination) {
        // 1) Find indices of the origin & destination within the route’s waypoints
        List<Waypoint> waypoints = fullRoute.getWaypointsList();
        int startIndex = findClosestWaypointIndex(waypoints, origin);
        int endIndex   = findClosestWaypointIndex(waypoints, destination);

        if (startIndex < 0 || endIndex < 0) {
            // If either is not found, return an empty Route or handle error as needed
            return Route.newBuilder().build();
        }

        // Ensure startIndex <= endIndex. If the route is reversed, you can swap or fail
        if (startIndex > endIndex) {
            int tmp = startIndex;
            startIndex = endIndex;
            endIndex = tmp;
        }

        // 2) Slice the sub-list of waypoints
        List<Waypoint> segmentWaypoints = waypoints.subList(startIndex, endIndex + 1);

        // 3) Recompute partial distances/durations so the segment starts at 0,0
        Route subRoute = recalcSegmentDistances(segmentWaypoints);

        return subRoute;
    }

    /**
     * Finds the waypoint index that is closest to 'target', or -1 if the route is empty.
     */
    private int findClosestWaypointIndex(List<Waypoint> waypoints, LatLng target) {
        if (waypoints.isEmpty()) return -1;
        double bestDist = Double.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < waypoints.size(); i++) {
            LatLng wpLoc = waypoints.get(i).getLocation();
            double dist = RouteService.calculateDistance(wpLoc, target);
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Rebuilds a sub-route from the extracted waypoints, recalculating distance/time
     * from the first waypoint so the segment starts at 0.0 distance and duration.
     */
    private Route recalcSegmentDistances(List<Waypoint> segmentWaypoints) {
        if (segmentWaypoints.isEmpty()) {
            return Route.newBuilder().build();
        }

        // We'll accumulate new waypoints in a builder,
        // recalculating distance/time for each step relative to the new “start”
        Route.Builder routeBuilder = Route.newBuilder();
        double totalDistance = 0.0;
        double totalDuration = 0.0;

        // The first waypoint is the “start” of this segment
        LatLng prevLoc = segmentWaypoints.getFirst().getLocation();
        // Add the first waypoint with distance/time = 0
        routeBuilder.addWaypoints(
                Waypoint.newBuilder()
                        .setLocation(prevLoc)
                        .setDistanceFromStartKm(0.0)
                        .setDurationFromStartMin(0.0)
        );

        // From the second waypoint onward, compute relative distances/time
        for (int i = 1; i < segmentWaypoints.size(); i++) {
            LatLng currLoc = segmentWaypoints.get(i).getLocation();
            double stepDistMeters = RouteService.calculateDistance(prevLoc, currLoc);
            double stepDistKm = stepDistMeters / 1000.0;

            // We'll estimate time difference from the original partial data
            // but you could also do a rough ratio from the existing step distance/time
            double stepDurationMin = segmentWaypoints.get(i).getDurationFromStartMin()
                    - segmentWaypoints.get(i - 1).getDurationFromStartMin();

            totalDistance += stepDistKm;
            totalDuration += stepDurationMin;

            Waypoint newWp = Waypoint.newBuilder()
                    .setLocation(currLoc)
                    .setDistanceFromStartKm(totalDistance)
                    .setDurationFromStartMin(totalDuration)
                    .build();

            routeBuilder.addWaypoints(newWp);
            prevLoc = currLoc;
        }

        routeBuilder.setTotalDistanceKm(totalDistance);
        routeBuilder.setTotalDurationMin(totalDuration);
        return routeBuilder.build();
    }
    /**
     * Attempts to merge the 'newPassengerRoute' into the 'existingRoute' by:
     *   1) Using Haversine-based approximation to evaluate all possible insert positions.
     *   2) Selecting the best approximate insertion index.
     *   3) Making a single Directions API call to confirm the final route.
     *
     * If the best final route exceeds ALLOWED_INCREASE_RATIO * originalDistance
     * or cannot be built, returns Optional.empty(). Otherwise, returns the merged Route.
     */
    public Optional<Route> tryMergeRoutes(Route existingRoute, Route newPassengerRoute) {

        // 1) Gather existing route's lat/lng points
        List<LatLng> existingWaypoints = toLatLngList(existingRoute.getWaypointsList());
        double originalDist = existingRoute.getTotalDistanceKm();

        // If the existing route's distance is zero or invalid, we can do a fallback:
        if (originalDist <= 0.0) {
            // Optionally: call the Directions API once to get a valid baseline
            // or treat it as very small but non-zero:
            originalDist = 0.001;
        }

        // 2) Gather new passenger waypoints
        List<LatLng> newSeg = toLatLngList(newPassengerRoute.getWaypointsList());

        // 3) Approximate insertion for each possible index
        double bestApproxDist = Double.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i <= existingWaypoints.size(); i++) {
            // approximate the total route distance if we insert newSeg at position i
            double approxDist = approximateInsertion(existingWaypoints, newSeg, i);
            if (approxDist > 0 && approxDist < bestApproxDist) {
                bestApproxDist = approxDist;
                bestIndex = i;
            }
        }

        // If even the best approximate cost is above threshold, skip the real API call
        if (bestApproxDist > originalDist * ALLOWED_INCREASE_RATIO) {
            return Optional.empty();
        }

        // 4) Splice at the best index and do ONE real call to confirm distance
        List<LatLng> finalCandidatePoints = spliceWaypoints(existingWaypoints, newSeg, bestIndex);
        Route finalRoute = getRouteFromWaypoints(finalCandidatePoints);

        double finalDist = finalRoute.getTotalDistanceKm();
        // 5) Check threshold with the real distance
        if (finalDist > 0 && finalDist <= originalDist * ALLOWED_INCREASE_RATIO) {
            return Optional.of(finalRoute);
        }

        // If it doesn't pass the threshold or no route found, return empty
        return Optional.empty();
    }

    /**
     * Converts repeated Waypoint messages to a simple List<LatLng>.
     */
    private List<LatLng> toLatLngList(List<Waypoint> waypoints) {
        List<LatLng> result = new ArrayList<>(waypoints.size());
        for (Waypoint w : waypoints) {
            result.add(w.getLocation());
        }
        return result;
    }

    /**
     * Splices 'newSegment' into 'original' at position 'insertIndex'.
     * e.g. original=[O0, O1, O2], newSeg=[N0, N1], insertIndex=1
     * => [O0, N0, N1, O1, O2]
     */
    private List<LatLng> spliceWaypoints(List<LatLng> original,
                                         List<LatLng> newSegment,
                                         int insertIndex) {
        List<LatLng> merged = new ArrayList<>();
        // everything up to insertIndex
        for (int i = 0; i < insertIndex && i < original.size(); i++) {
            merged.add(original.get(i));
        }
        // add new segment
        merged.addAll(newSegment);
        // add remainder
        for (int i = insertIndex; i < original.size(); i++) {
            merged.add(original.get(i));
        }
        return merged;
    }

    /**
     * Approximates the total distance of 'original' route after inserting 'newSeg'
     * at position 'insertIndex' using a simple method (e.g. Haversine for consecutive points).
     *
     * @return approximate total distance in kilometers
     */
    private double approximateInsertion(List<LatLng> original,
                                        List<LatLng> newSeg,
                                        int insertIndex) {
        // 1) Build a temporary spliced list
        List<LatLng> temp = spliceWaypoints(original, newSeg, insertIndex);
        // 2) Sum up consecutive pairs with Haversine distance
        return approximateRouteDistance(temp);
    }

    /**
     * Computes approximate route distance (km) by summing consecutive pairs with Haversine.
     */
    private double approximateRouteDistance(List<LatLng> points) {
        if (points.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            total += RouteService.calculateDistance(points.get(i), points.get(i+1));
        }
        return total;
    }

    /**
     * Given an ordered list of LatLng, calls the Directions API to retrieve
     * an actual driving route. Returns a Protobuf 'Route' message with:
     *  - total_distance_km
     *  - total_duration_min
     *  - repeated Waypoint (with incremental distance/time from start)
     *
     * If no route is found, returns an empty Route message.
     */
    private Route getRouteFromWaypoints(List<LatLng> waypoints) {
        if (waypoints.size() < 2) {
            // Not enough points to form a real route
            return Route.newBuilder().build();
        }

        // Convert to com.google.maps.model.LatLng
        com.google.maps.model.LatLng origin = toModelLatLng(waypoints.getFirst());
        com.google.maps.model.LatLng destination = toModelLatLng(waypoints.getLast());

        // The "middle" waypoints
        List<com.google.maps.model.LatLng> viaPoints = new ArrayList<>();
        for (int i = 1; i < waypoints.size() - 1; i++) {
            viaPoints.add(toModelLatLng(waypoints.get(i)));
        }

        // Build Directions request
        DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.DRIVING);

        if (!viaPoints.isEmpty()) {
            request.waypoints(viaPoints.toArray(new com.google.maps.model.LatLng[0]));
        }

        // Execute the request synchronously
        try {
            DirectionsResult result = request.await();
            if (result.routes == null || result.routes.length == 0) {
                // No route found
                return Route.newBuilder().build();
            }

            // We'll just take the first route
            DirectionsRoute bestRoute = result.routes[0];

            // Construct the final Route proto
            return buildRouteProto(bestRoute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Helper: converts from com.google.type.LatLng to com.google.maps.model.LatLng */
    private com.google.maps.model.LatLng toModelLatLng(LatLng latLng) {
        return new com.google.maps.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
    }

    /**
     * Build a Protobuf Route from a DirectionsRoute.
     * We sum leg distances, accumulate a list of "waypoints" based on each leg's end.
     * Optionally, you could go deeper into each step if you want more detailed waypoints.
     */
    private Route buildRouteProto(DirectionsRoute directionsRoute) {
        double totalDistanceKm = 0.0;
        double totalDurationMin = 0.0;

        Route.Builder routeBuilder = Route.newBuilder();

        if (directionsRoute.legs == null || directionsRoute.legs.length == 0) {
            return routeBuilder.build();
        }

        for (DirectionsLeg leg : directionsRoute.legs) {
            // Leg distance and duration
            double legDistKm = leg.distance.inMeters / 1000.0;   // convert meters -> km
            double legDurMin = leg.duration.inSeconds / 60.0;    // convert seconds -> minutes

            // Accumulate into total route so far
            totalDistanceKm += legDistKm;
            totalDurationMin += legDurMin;

            // We'll place a Waypoint at the leg's end location
            if (leg.endLocation != null) {
                Waypoint.Builder wp = Waypoint.newBuilder();

                wp.setLocation(
                        com.google.type.LatLng.newBuilder()
                                .setLatitude(leg.endLocation.lat)
                                .setLongitude(leg.endLocation.lng)
                                .build()
                );
                wp.setDistanceFromStartKm(totalDistanceKm);
                wp.setDurationFromStartMin(totalDurationMin);

                routeBuilder.addWaypoints(wp.build());
            }
        }

        routeBuilder.setTotalDistanceKm(totalDistanceKm);
        routeBuilder.setTotalDurationMin(totalDurationMin);

        return routeBuilder.build();
    }


}
package org.example.goeverywhere.server.service.routing;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.example.goeverywhere.protocol.grpc.WaypointMetadata;
import org.example.goeverywhere.protocol.grpc.WaypointType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Demonstrates a single-call approach to merging routes:
 * 1) Use haversine approximate distance for each candidate.
 * 2) Pick the best insertion.
 * 3) Make exactly one Directions API call for that candidate.
 * 4) Re-inject metadata from both routes.
 */
@Service("googleRouteService")
public class GoogleMapsRouteService implements RouteService {

    private static final double ALLOWED_INCREASE_RATIO = 1.30; // up to +30%
    private final GeoApiContext geoApiContext;

    @Autowired
    public GoogleMapsRouteService(@Value("${google.geo.api.key}") String googleApiKey) {
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();
    }

    @Override
    public Route generateRoute(LatLng start, LatLng destination, String riderSessionId) {
        // Basic single-route generation, if needed
        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(start.getLatitude() + "," + start.getLongitude())
                    .destination(destination.getLatitude() + "," + destination.getLongitude())
                    .await();
            if (result.routes == null || result.routes.length == 0) {
                return Route.newBuilder().build();
            }
            return buildRouteProto(result.routes[0], riderSessionId);
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
     * The "one Directions API call" approach:
     * 1) Convert existing + second routes to lat/lon lists & metadata
     * 2) Enumerate (i, j), computing approximate (haversine) total distance.
     * 3) Choose best candidate
     * 4) Make one call to getRouteFromWaypoints(...) for that candidate
     * 5) Re-inject metadata, ensure final distance <= ALLOWED_INCREASE_RATIO * originalDist
     */
    @Override
    public Optional<Route> tryMergeRoutes(Route existingRoute, Route secondRoute) {
        double originalDist = Math.max(existingRoute.getTotalDistanceKm(), 0.001);

        // Build metadata maps
        Map<LatLng, List<WaypointMetadata>> existingMap = buildMetadataMap(existingRoute);
        Map<LatLng, List<WaypointMetadata>> secondMap = buildMetadataMap(secondRoute);

        // Convert to lists of lat/lon
        List<LatLng> existingPoints = new ArrayList<>(existingMap.keySet());
        List<LatLng> secondPoints = new ArrayList<>(secondMap.keySet());
        if (secondPoints.size() < 2) {
            return Optional.empty(); // second route not meaningful
        }

        double bestApproxDist = Double.MAX_VALUE;
        List<LatLng> bestCandidate = null;

        // Enumerate all possible i, j insertion
        for (int i = 0; i <= existingPoints.size(); i++) {
            for (int j = i; j <= existingPoints.size(); j++) {
                // Build a spliced route with second route from i..j
                List<LatLng> candidate = buildCandidate(existingPoints, secondPoints, i, j);
                candidate = removeConsecutiveDuplicates(candidate);

                // Approx distance by simple Haversine pairwise sum
                double approxDist = approximateRouteDistance(candidate);
                if (approxDist > 0 && approxDist < bestApproxDist) {
                    bestApproxDist = approxDist;
                    bestCandidate = candidate;
                }
            }
        }

        // If no candidate, or approximate best route is still too big, skip
        if (bestCandidate == null || bestApproxDist > originalDist * ALLOWED_INCREASE_RATIO) {
            return Optional.empty();
        }

        // 1 Directions API call for bestCandidate
        Route rawMergedRoute = getRouteFromWaypoints(bestCandidate);
        double finalDist = rawMergedRoute.getTotalDistanceKm();
        if (finalDist <= 0 || finalDist > originalDist * ALLOWED_INCREASE_RATIO) {
            return Optional.empty();
        }

        // Re-inject metadata
        Route finalRoute = reInjectMetadata(rawMergedRoute, existingMap, secondMap);
        return Optional.of(finalRoute);
    }

    /**
     * Build a single lat/lon candidate by inserting secondPoints
     * at i..j in existingPoints. For simplicity, we do:
     * [0..i) of existing -> secondPoints -> [i..j) -> [j..end].
     * <p>
     * You can refine to handle partial vs full second route, etc.
     */
    private List<LatLng> buildCandidate(List<LatLng> existingPoints, List<LatLng> secondPoints, int i, int j) {
        List<LatLng> result = new ArrayList<>();

        // Add existing [0..i)
        for (int idx = 0; idx < i && idx < existingPoints.size(); idx++) {
            result.add(existingPoints.get(idx));
        }
        // Add entire second route
        result.addAll(secondPoints);
        // Add existing [i..j)
        for (int idx = i; idx < j && idx < existingPoints.size(); idx++) {
            result.add(existingPoints.get(idx));
        }
        // Add existing [j..end)
        for (int idx = j; idx < existingPoints.size(); idx++) {
            result.add(existingPoints.get(idx));
        }
        return result;
    }

    /**
     * Deduplicate consecutive lat/lon points that are exactly the same
     */
    private List<LatLng> removeConsecutiveDuplicates(List<LatLng> points) {
        if (points.size() < 2) return points;
        List<LatLng> deduped = new ArrayList<>();
        deduped.add(points.getFirst());
        for (int i = 1; i < points.size(); i++) {
            LatLng prev = deduped.getLast();
            LatLng curr = points.get(i);
            if (!prev.equals(curr)) {
                deduped.add(curr);
            }
        }
        return deduped;
    }



    /**
     * Basic pairwise haversine sum for approximate route distance in meters -> convert to km
     */
    private double approximateRouteDistance(List<LatLng> points) {
        if (points.size() < 2) return 0.0;
        double totalMeters = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            totalMeters += RouteService.calculateDistance(points.get(i), points.get(i + 1));
        }
        return totalMeters / 1000.0; // in km
    }

    /**
     * Convert lat/lon list to a real route using Google Directions, as in your existing code.
     */
    private Route getRouteFromWaypoints(List<LatLng> latLngs) {
        if (latLngs.size() < 2) {
            return Route.newBuilder().build();
        }
        com.google.maps.model.LatLng origin = toModelLatLng(latLngs.getFirst());
        com.google.maps.model.LatLng dest = toModelLatLng(latLngs.getLast());

        List<com.google.maps.model.LatLng> viaPoints = new ArrayList<>();
        for (int i = 1; i < latLngs.size() - 1; i++) {
            viaPoints.add(toModelLatLng(latLngs.get(i)));
        }

        try {
            DirectionsApiRequest req = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(dest)
                    .mode(TravelMode.DRIVING);
            if (!viaPoints.isEmpty()) {
                req.waypoints(viaPoints.toArray(new com.google.maps.model.LatLng[0]));
            }

            DirectionsResult result = req.await();
            if (result.routes == null || result.routes.length == 0) {
                return Route.newBuilder().build();
            }
            // will inject the metadata later
            return buildRouteProto(result.routes[0], null);
        } catch (Exception e) {
            throw new RuntimeException("Error building route from waypoints", e);
        }
    }

    /**
     * Re-inject metadata: we rely on findNearest() to match final lat/lons to the original sets.
     */
    private Route reInjectMetadata(Route mergedRaw,
                                   Map<LatLng, List<WaypointMetadata>> existingMap,
                                   Map<LatLng, List<WaypointMetadata>> secondMap) {
        Route.Builder builder = mergedRaw.toBuilder();
        builder.clearWaypoints();

        for (Waypoint wp : mergedRaw.getWaypointsList()) {
            LatLng loc = wp.getLocation();
            LatLng bestKey = findNearest(loc, existingMap.keySet(), secondMap.keySet());

            List<WaypointMetadata> combined = new ArrayList<>();
            if (bestKey != null) {
                if (existingMap.containsKey(bestKey)) {
                    combined.addAll(existingMap.get(bestKey));
                }
                if (secondMap.containsKey(bestKey)) {
                    combined.addAll(secondMap.get(bestKey));
                }
            }

            Waypoint.Builder wpb = wp.toBuilder();
            wpb.clearWaypointMetadata();
            wpb.addAllWaypointMetadata(combined);
            builder.addWaypoints(wpb.build());
        }
        return builder.build();
    }

    private LatLng findNearest(LatLng actual, Set<LatLng>... sets) {
        double bestDist = Double.MAX_VALUE;
        LatLng best = null;
        for (Set<LatLng> s : sets) {
            for (LatLng cand : s) {
                double d = RouteService.calculateDistance(actual, cand);
                if (d < bestDist) {
                    bestDist = d;
                    best = cand;
                }
            }
        }
        // threshold of 50 meters, say
        if (bestDist < 0.05 * 1000) {
            return best;
        }
        return null;
    }

    /**
     * Summarize lat/lon + repeated metadata from a Route into a LinkedHashMap
     */
    private Map<LatLng, List<WaypointMetadata>> buildMetadataMap(Route route) {
        Map<LatLng, List<WaypointMetadata>> map = new LinkedHashMap<>();
        for (Waypoint wp : route.getWaypointsList()) {
            LatLng loc = wp.getLocation();
            map.computeIfAbsent(loc, k -> new ArrayList<>())
                    .addAll(wp.getWaypointMetadataList());
        }
        return map;
    }

    private com.google.maps.model.LatLng toModelLatLng(LatLng latLng) {
        return new com.google.maps.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
    }

    private Route buildRouteProto(DirectionsRoute directionsRoute, String riderId) {
        double totalDistanceKm = 0.0;
        double totalDurationMin = 0.0;
        Route.Builder rb = Route.newBuilder();
        if (directionsRoute.legs == null || directionsRoute.legs.length == 0) {
            return rb.build();
        }
        for (int i = 0; i < directionsRoute.legs.length; i++) {
            DirectionsLeg leg = directionsRoute.legs[i];
            double legDistKm = leg.distance.inMeters / 1000.0;
            double legDurMin = leg.duration.inSeconds / 60.0;
            totalDistanceKm += legDistKm;
            totalDurationMin += legDurMin;
            if (leg.endLocation != null) {
                Waypoint.Builder wpb = Waypoint.newBuilder()
                        .setLocation(
                                LatLng.newBuilder()
                                        .setLatitude(leg.endLocation.lat)
                                        .setLongitude(leg.endLocation.lng)
                        )
                        .setDistanceFromStartKm(totalDistanceKm)
                        .setDurationFromStartMin(totalDurationMin);
                if(riderId != null) {
                    if (i == 0) {
                        wpb.addWaypointMetadata(WaypointMetadata.newBuilder().setRiderId(riderId).setWaypointType(WaypointType.ORIGIN).build());
                    }
                    if(i == directionsRoute.legs.length - 1) {
                        wpb.addWaypointMetadata(WaypointMetadata.newBuilder().setRiderId(riderId).setWaypointType(WaypointType.DESTINATION).build());
                    }
                }
                rb.addWaypoints(wpb.build());
            }
        }
        rb.setTotalDistanceKm(totalDistanceKm);
        rb.setTotalDurationMin(totalDurationMin);
        return rb.build();
    }

    /**
     * This or other existing methods you might have in your code for measure
     */
    public static double calculateDistance(LatLng a, LatLng b) {
        double R = 6371e3; // Earth in meters
        double lat1 = Math.toRadians(a.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon2 = Math.toRadians(b.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(
                sinLat * sinLat +
                        Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon));
        return R * c; // in meters
    }
}

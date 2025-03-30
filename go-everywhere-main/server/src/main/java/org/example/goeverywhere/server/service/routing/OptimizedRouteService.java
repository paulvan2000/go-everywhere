package org.example.goeverywhere.server.service.routing;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.main;
import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.Route;
import org.example.goeverywhere.protocol.grpc.Waypoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("optimizedRouteService")
public class OptimizedRouteService implements RouteService {
    // Ensure the native OR‑Tools library is loaded.
    static {
        // If your native library is now named "google-ortools-native", update this call.
        System.loadLibrary("google-ortools-native");
    }

    /**
     * Generates a route from start to destination using OR‑Tools for a single ride.
     */
    @Override
    public Route generateRoute(LatLng start, LatLng destination, String riderSessionId) {
        final int numLocations = 2;
        final int numVehicles = 1;
        final int depot = 0;

        // Build a distance matrix (in meters) for 2 nodes.
        long[][] distanceMatrix = new long[numLocations][numLocations];
        double distance = calculateDistance(start, destination);
        distanceMatrix[0][1] = (long) distance;
        distanceMatrix[1][0] = (long) distance; // symmetric matrix
        distanceMatrix[0][0] = 0;
        distanceMatrix[1][1] = 0;

        // Create routing manager and model.
        RoutingIndexManager manager = new RoutingIndexManager(numLocations, numVehicles, depot);
        RoutingModel routing = new RoutingModel(manager);

        // Register transit callback.
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Create search parameters.
        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();

        // Solve the routing problem.
        Assignment solution = routing.solveWithParameters(searchParameters);
        if (solution == null) {
            throw new RuntimeException("No solution found for the route optimization problem.");
        }

        // Extract the route indices.
        List<Integer> routeIndices = new ArrayList<>();
        long index = routing.start(0);
        while (!routing.isEnd(index)) {
            routeIndices.add(manager.indexToNode(index));
            index = solution.value(routing.nextVar(index));
        }
        routeIndices.add(manager.indexToNode(index));

        // Build the Route proto message.
        Route.Builder routeBuilder = Route.newBuilder();
        double cumulativeDistance = 0.0;
        for (int i = 0; i < routeIndices.size(); i++) {
            if (i > 0) {
                cumulativeDistance += distanceMatrix[routeIndices.get(i - 1)][routeIndices.get(i)];
            }
            Waypoint.Builder waypointBuilder = Waypoint.newBuilder();
            // For a simple ride, node 0 is the start; node 1 is the destination.
            LatLng loc = (routeIndices.get(i) == 0) ? start : destination;
            waypointBuilder.setLocation(loc);
            waypointBuilder.setDistanceFromStartKm(cumulativeDistance / 1000.0);
            waypointBuilder.setDurationFromStartMin((cumulativeDistance / 1000.0) * 2);
            routeBuilder.addWaypoints(waypointBuilder);
        }
        routeBuilder.setTotalDistanceKm(cumulativeDistance / 1000.0);
        routeBuilder.setTotalDurationMin((cumulativeDistance / 1000.0) * 2);

        return routeBuilder.build();
    }

    /**
     * Generates a merged (optimized) route for multiple ride requests.
     * @param depot    The starting location (e.g., driver's current location).
     * @param pickups  List of pickup locations.
     * @param dropoffs List of corresponding dropoff locations.
     * @return A Route proto message representing the merged route.
     */
    public Route generateMergedRoute(LatLng depot, List<LatLng> pickups, List<LatLng> dropoffs) {
        int numRequests = pickups.size();
        int numNodes = 1 + 2 * numRequests; // 1 depot + pickups + dropoffs
        int numVehicles = 1;
        int depotIndex = 0;

        // Build distance matrix for all nodes.
        long[][] distanceMatrix = new long[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            LatLng locI = getLocationForNode(i, depot, pickups, dropoffs);
            for (int j = 0; j < numNodes; j++) {
                LatLng locJ = getLocationForNode(j, depot, pickups, dropoffs);
                distanceMatrix[i][j] = (long) calculateDistance(locI, locJ);
            }
        }

        // Create routing index manager and model.
        RoutingIndexManager manager = new RoutingIndexManager(numNodes, numVehicles, depotIndex);
        RoutingModel routing = new RoutingModel(manager);

        // Register transit callback.
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add pickup and delivery constraints.
        // For each ride, nodes 1...numRequests are pickups, nodes numRequests+1...2*numRequests are dropoffs.
        for (int i = 0; i < numRequests; i++) {
            int pickupNode = 1 + i;
            int dropoffNode = 1 + numRequests + i;
           // nodeToIndex(...) returns a long, so cast to int:
            int pickupIndex = (int) manager.nodeToIndex(pickupNode);
            int dropoffIndex = (int) manager.nodeToIndex(dropoffNode);

            routing.addPickupAndDelivery(pickupIndex, dropoffIndex);
            // Then use makeEquality(...) or other constraints as needed.
            routing.solver().addConstraint(
                    routing.solver().makeEquality(routing.vehicleVar(pickupIndex), routing.vehicleVar(dropoffIndex))
            );
        }

        // Create search parameters.
        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();

        // Solve the routing problem.
        Assignment solution = routing.solveWithParameters(searchParameters);
        if (solution == null) {
            throw new RuntimeException("No merged solution found.");
        }

        // Extract the route indices.
        List<Integer> routeIndices = new ArrayList<>();
        long index = routing.start(0);
        while (!routing.isEnd(index)) {
            routeIndices.add(manager.indexToNode(index));
            index = solution.value(routing.nextVar(index));
        }
        routeIndices.add(manager.indexToNode(index));

        // Build the Route proto message.
        Route.Builder routeBuilder = Route.newBuilder();
        double cumulativeDistance = 0.0;
        for (int i = 0; i < routeIndices.size(); i++) {
            if (i > 0) {
                cumulativeDistance += distanceMatrix[routeIndices.get(i - 1)][routeIndices.get(i)];
            }
            Waypoint.Builder waypointBuilder = Waypoint.newBuilder();
            LatLng loc = getLocationForNode(routeIndices.get(i), depot, pickups, dropoffs);
            waypointBuilder.setLocation(loc);
            waypointBuilder.setDistanceFromStartKm(cumulativeDistance / 1000.0);
            waypointBuilder.setDurationFromStartMin((cumulativeDistance / 1000.0) * 2);
            routeBuilder.addWaypoints(waypointBuilder);
        }
        routeBuilder.setTotalDistanceKm(cumulativeDistance / 1000.0);
        routeBuilder.setTotalDurationMin((cumulativeDistance / 1000.0) * 2);

        return routeBuilder.build();
    }

    /**
     * Helper method: maps a node index to its corresponding LatLng.
     * Node 0: depot.
     * Nodes 1 to N: pickups.
     * Nodes N+1 to 2N: dropoffs.
     */
    private LatLng getLocationForNode(int nodeIndex, LatLng depot, List<LatLng> pickups, List<LatLng> dropoffs) {
        if (nodeIndex == 0) {
            return depot;
        }
        int n = pickups.size();
        if (nodeIndex >= 1 && nodeIndex <= n) {
            return pickups.get(nodeIndex - 1);
        } else {
            return dropoffs.get(nodeIndex - n - 1);
        }
    }

    @Override
    public Optional<Route> tryMergeRoutes(Route existingRoute, Route newPassengerRoute) {
        // TODO: Implement merging logic between two routes if needed.
        return Optional.empty();
    }

    @Override
    public Route getRouteSegment(Route route, LatLng start, LatLng destination) {
        // For now, return the full route.
        return route;
    }

    /**
     * Calculates the haversine distance between two LatLng points in meters.
     */
    private double calculateDistance(LatLng a, LatLng b) {
        final double R = 6371e3; // Earth's radius in meters
        double lat1 = Math.toRadians(a.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon2 = Math.toRadians(b.getLongitude());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);
        double aVal = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(aVal), Math.sqrt(1 - aVal));
        return R * c;
    }
}

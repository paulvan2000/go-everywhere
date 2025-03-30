// src/main/java/org/example/goeverywhere/server/service/routing/OptimizedRouteService.java
package org.example.goeverywhere.server.service.routing;

import com.google.ortools.constraintsolver.*; // Import necessary OR-Tools classes
import com.google.type.LatLng;
import org.example.goeverywhere.protocol.grpc.*; // Import proto classes
import org.springframework.data.util.Pair; // Using Spring's Pair for simplicity
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("optimizedRouteService")
public class OptimizedRouteService implements RouteService {
    static {
        try {
            System.loadLibrary("google-ortools-native"); // Ensure correct library name
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native OR-Tools library failed to load.\n" + e);
            System.exit(1);
        }
    }

    // --- Updated generateRoute for single ride with metadata ---
    @Override
    public Route generateRoute(LatLng start, LatLng destination, String riderSessionId) {
        final int NUM_LOCATIONS = 2;
        final int NUM_VEHICLES = 1;
        final int DEPOT_NODE_INDEX = 0; // Corresponds to 'start' location
        final int DEST_NODE_INDEX = 1; // Corresponds to 'destination' location

        long[][] distanceMatrix = buildDistanceMatrix(List.of(start, destination));

        RoutingIndexManager manager = new RoutingIndexManager(NUM_LOCATIONS, NUM_VEHICLES, DEPOT_NODE_INDEX);
        RoutingModel routing = new RoutingModel(manager);

        final int transitCallbackIndex = registerTransitCallback(routing, manager, distanceMatrix);
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        Assignment solution = solve(routing);

        Route.Builder routeBuilder = Route.newBuilder();
        List<Integer> routeNodeIndices = extractRouteNodeIndices(routing, manager, solution, 0);

        Map<Integer, LatLng> nodeIndexToLocation = Map.of(
                DEPOT_NODE_INDEX, start,
                DEST_NODE_INDEX, destination
        );

        // Build Route proto with metadata
        buildRouteProtoFromSolution(routeBuilder, routeNodeIndices, nodeIndexToLocation,
                distanceMatrix, Map.of(DEST_NODE_INDEX, riderSessionId), // Map node index -> rider ID
                Map.of(DEPOT_NODE_INDEX, WaypointType.ORIGIN, DEST_NODE_INDEX, WaypointType.DESTINATION) // Map node index -> type
        );

        return routeBuilder.build();
    }

    // --- Structure to hold location and rider ID for merging ---
    private static class LocationInfo {
        final LatLng location;
        final String riderId;
        final WaypointType type;

        LocationInfo(LatLng location, String riderId, WaypointType type) {
            this.location = location;
            this.riderId = riderId;
            this.type = type;
        }
    }

    // --- Updated generateMergedRoute with metadata ---
// src/main/java/org/example/goeverywhere/server/service/routing/OptimizedRouteService.java

// ... other imports ...
import com.google.ortools.constraintsolver.RoutingDimension; // <<< Import RoutingDimension

// ... inside the OptimizedRouteService class ...

    /**
     * Generates a merged (optimized) route for multiple ride requests including metadata.
     * @param depotLocation The starting location (e.g., driver's current location).
     * @param pickups  List of Pairs containing RiderId and Pickup Location.
     * @param dropoffs List of Pairs containing RiderId and Dropoff Location.
     * @return A Route proto message representing the merged route with metadata.
     */
    public Route generateMergedRoute(LatLng depotLocation,
                                     List<Pair<String, LatLng>> pickups, // Pair<RiderId, Location>
                                     List<Pair<String, LatLng>> dropoffs) { // Pair<RiderId, Location>

        if (pickups.isEmpty() || pickups.size() != dropoffs.size()) {
            throw new IllegalArgumentException("Pickups and dropoffs must be non-empty and have matching sizes.");
        }

        int numRequests = pickups.size();
        // Node Mapping: 0=Depot, 1..N=Pickups, N+1..2N=Dropoffs
        final int DEPOT_NODE_INDEX = 0;
        int numNodes = 1 + 2 * numRequests;
        int numVehicles = 1; // Assuming a single vehicle/driver for the batch

        // --- 1. Consolidate Location Information ---
        List<LocationInfo> allLocations = new ArrayList<>(numNodes);
        allLocations.add(new LocationInfo(depotLocation, null, WaypointType.UNDEFINED)); // Depot Node 0

        // Maps for easy lookup during constraint/metadata building
        Map<Integer, LocationInfo> nodeIndexToInfoMap = new HashMap<>();
        nodeIndexToInfoMap.put(DEPOT_NODE_INDEX, allLocations.get(DEPOT_NODE_INDEX));
        Map<String, Integer> riderPickupNodeIndex = new HashMap<>();
        Map<String, Integer> riderDropoffNodeIndex = new HashMap<>();

        // Add pickups (Nodes 1 to N)
        for (int i = 0; i < numRequests; i++) {
            Pair<String, LatLng> pickup = pickups.get(i);
            int pickupNodeIndex = 1 + i;
            LocationInfo pickupInfo = new LocationInfo(pickup.getSecond(), pickup.getFirst(), WaypointType.ORIGIN);
            allLocations.add(pickupInfo);
            nodeIndexToInfoMap.put(pickupNodeIndex, pickupInfo);
            riderPickupNodeIndex.put(pickup.getFirst(), pickupNodeIndex);
        }
        // Add dropoffs (Nodes N+1 to 2N)
        for (int i = 0; i < numRequests; i++) {
            Pair<String, LatLng> dropoff = dropoffs.get(i);
            int dropoffNodeIndex = 1 + numRequests + i;
            LocationInfo dropoffInfo = new LocationInfo(dropoff.getSecond(), dropoff.getFirst(), WaypointType.DESTINATION);
            allLocations.add(dropoffInfo);
            nodeIndexToInfoMap.put(dropoffNodeIndex, dropoffInfo);
            riderDropoffNodeIndex.put(dropoff.getFirst(), dropoffNodeIndex);
        }

        // --- 2. Build Distance Matrix ---
        long[][] distanceMatrix = buildDistanceMatrix(allLocations.stream().map(li -> li.location).collect(Collectors.toList()));

        // --- 3. Setup OR-Tools Routing Model ---
        RoutingIndexManager manager = new RoutingIndexManager(numNodes, numVehicles, DEPOT_NODE_INDEX);
        RoutingModel routing = new RoutingModel(manager);

        // --- 4. Define Cost (Transit Callback) ---
        final int transitCallbackIndex = registerTransitCallback(routing, manager, distanceMatrix);
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // --- 5. Add Dimension for Distance Tracking (BEFORE constraints) ---
        // This allows us to constrain based on accumulated distance.
        final String distanceDimensionName = "distance";
        routing.addDimension(
                transitCallbackIndex, // Use the same callback for distance accumulation
                0, // slack_max: Allow no waiting time at nodes based on distance
                Long.MAX_VALUE, // capacity: Effectively unlimited route distance
                false, // start_cumul_to_zero: Distance starts at 0 at the depot (node 0)
                distanceDimensionName
        );
        // Get a reference to the newly added dimension
        RoutingDimension distanceDimension = routing.getDimensionOrDie(distanceDimensionName);

        // --- 6. Add Constraints ---
        for (String riderId : riderPickupNodeIndex.keySet()) {
            // Get the internal OR-Tools indices for pickup/dropoff nodes
            long pickupNodeManagerIndex = manager.nodeToIndex(riderPickupNodeIndex.get(riderId));
            long dropoffNodeManagerIndex = manager.nodeToIndex(riderDropoffNodeIndex.get(riderId));

            // a) Define the Pickup & Delivery relationship
            routing.addPickupAndDelivery(pickupNodeManagerIndex, dropoffNodeManagerIndex);

            // b) Ensure Pickup occurs before Dropoff (using the distance dimension)
            routing.solver().addConstraint(routing.solver().makeLessOrEqual(
                    distanceDimension.cumulVar(pickupNodeManagerIndex), // Accumulated distance at pickup
                    distanceDimension.cumulVar(dropoffNodeManagerIndex)  // Accumulated distance at dropoff
            ));

            // c) Ensure the same vehicle serves both pickup and dropoff (trivial for 1 vehicle, essential for multi-vehicle)
            routing.solver().addConstraint(routing.solver().makeEquality(
                    routing.vehicleVar(pickupNodeManagerIndex),
                    routing.vehicleVar(dropoffNodeManagerIndex)
            ));

            // d) OPTIONAL: Add Time Window Constraints here if needed, using a 'time' dimension
            // routing.addDimension(...) for time
            // timeDimension.cumulVar(pickupNodeManagerIndex).greaterOrEqual(pickup_ready_time);
            // timeDimension.cumulVar(pickupNodeManagerIndex).lessOrEqual(pickup_due_time);
            // ... similar for dropoff ...
        }

        // --- 7. Set Search Parameters and Solve ---
        Assignment solution = solve(routing); // Uses helper method

        // --- 8. Extract Route and Build Proto ---
        Route.Builder routeBuilder = Route.newBuilder();
        // Extract the sequence of node indices visited by vehicle 0
        List<Integer> routeNodeIndices = extractRouteNodeIndices(routing, manager, solution, 0);

        // Prepare maps for easier metadata lookup during proto building
        Map<Integer, String> nodeToRiderIdMap = nodeIndexToInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().riderId != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().riderId));

        Map<Integer, WaypointType> nodeToTypeMap = nodeIndexToInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().type != WaypointType.UNDEFINED)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().type));

        Map<Integer, LatLng> nodeToLocationMap = nodeIndexToInfoMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().location));

        // Use helper to build the final Route proto object with waypoints and metadata
        buildRouteProtoFromSolution(routeBuilder, routeNodeIndices, nodeToLocationMap,
                distanceMatrix, nodeToRiderIdMap, nodeToTypeMap);

        // --- 9. Return the Result ---
        return routeBuilder.build();
    }

    // --- Helper to build Route proto from OR-Tools solution ---
    private void buildRouteProtoFromSolution(Route.Builder routeBuilder,
                                             List<Integer> routeNodeIndices,
                                             Map<Integer, LatLng> nodeIndexToLocation,
                                             long[][] distanceMatrix,
                                             Map<Integer, String> nodeIndexToRiderId,
                                             Map<Integer, WaypointType> nodeIndexToWaypointType) {
        double cumulativeDistanceKm = 0.0;
        // TODO: Calculate duration more accurately if possible (e.g., using a time matrix callback in OR-Tools)
        // Simple approximation: factor * distance
        double speedFactorForDuration = 2.0; // minutes per km (adjust based on expected average speed)

        for (int i = 0; i < routeNodeIndices.size(); i++) {
            int currentNodeIndex = routeNodeIndices.get(i);
            LatLng currentLocation = nodeIndexToLocation.get(currentNodeIndex);

            if (i > 0) {
                int previousNodeIndex = routeNodeIndices.get(i - 1);
                cumulativeDistanceKm += (distanceMatrix[previousNodeIndex][currentNodeIndex] / 1000.0);
            }

            Waypoint.Builder waypointBuilder = Waypoint.newBuilder()
                    .setLocation(currentLocation)
                    .setDistanceFromStartKm(cumulativeDistanceKm)
                    .setDurationFromStartMin(cumulativeDistanceKm * speedFactorForDuration); // Approximate duration

            // Add Metadata
            String riderId = nodeIndexToRiderId.get(currentNodeIndex);
            WaypointType type = nodeIndexToWaypointType.get(currentNodeIndex);

            if (riderId != null && type != null && type != WaypointType.UNDEFINED) {
                waypointBuilder.addWaypointMetadata(WaypointMetadata.newBuilder()
                        .setRiderId(riderId)
                        .setWaypointType(type)
                        .build());
            }
            // Add metadata for depot if needed (currently skipped as type is UNDEFINED)

            routeBuilder.addWaypoints(waypointBuilder.build());
        }
        routeBuilder.setTotalDistanceKm(cumulativeDistanceKm);
        routeBuilder.setTotalDurationMin(cumulativeDistanceKm * speedFactorForDuration); // Approx total duration
    }


    // --- Helper Methods for OR-Tools setup ---

    private long[][] buildDistanceMatrix(List<LatLng> locations) {
        int numLocations = locations.size();
        long[][] distanceMatrix = new long[numLocations][numLocations];
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    distanceMatrix[i][j] = (long) calculateDistance(locations.get(i), locations.get(j)); // distance in meters
                }
            }
        }
        return distanceMatrix;
    }

    private int registerTransitCallback(RoutingModel routing, RoutingIndexManager manager, long[][] distanceMatrix) {
        return routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            // Bounds check for safety
            if (fromNode >= 0 && fromNode < distanceMatrix.length && toNode >= 0 && toNode < distanceMatrix.length) {
                return distanceMatrix[fromNode][toNode];
            }
            return Long.MAX_VALUE; // Indicate unreachable if indices are out of bounds
        });
    }

    private Assignment solve(RoutingModel routing) {
        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                // .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH) // Consider adding for better quality
                // .setTimeLimit(Duration.newBuilder().setSeconds(5)) // Add time limit if needed
                .build();
        Assignment solution = routing.solveWithParameters(searchParameters);
        if (solution == null) {
            throw new RuntimeException("No solution found for the route optimization problem.");
        }
        return solution;
    }

    private List<Integer> extractRouteNodeIndices(RoutingModel routing, RoutingIndexManager manager, Assignment solution, int vehicleId) {
        List<Integer> routeIndices = new ArrayList<>();
        long index = routing.start(vehicleId);
        while (!routing.isEnd(index)) {
            routeIndices.add(manager.indexToNode(index));
            index = solution.value(routing.nextVar(index));
        }
        routeIndices.add(manager.indexToNode(index)); // Add the end node too
        return routeIndices;
    }

    // --- tryMergeRoutes implementation using generateMergedRoute ---
    @Override
    public Optional<Route> tryMergeRoutes(Route existingRoute, Route newPassengerRoute) {
        if (existingRoute == null || newPassengerRoute == null ||
                existingRoute.getWaypointsCount() == 0 || newPassengerRoute.getWaypointsCount() < 2) {
            return Optional.empty();
        }

        // --- Extract required info ---
        // 1. Driver's current location (Assume it's the *first* waypoint of the existing route for this example)
        //    TODO: Get actual driver location from UserRegistry or context if available
        LatLng driverCurrentLocation = existingRoute.getWaypoints(0).getLocation();

        // 2. Extract pickups and dropoffs with Rider IDs from both routes
        List<Pair<String, LatLng>> allPickups = new ArrayList<>();
        List<Pair<String, LatLng>> allDropoffs = new ArrayList<>();

        extractPickupsDropoffs(existingRoute, allPickups, allDropoffs);
        extractPickupsDropoffs(newPassengerRoute, allPickups, allDropoffs);

        if (allPickups.isEmpty()) {
            System.err.println("tryMergeRoutes: No pickup/dropoff metadata found in routes.");
            return Optional.empty(); // Cannot merge without knowing which points are P/D
        }

        try {
            // 3. Call generateMergedRoute with combined data
            Route mergedRoute = generateMergedRoute(driverCurrentLocation, allPickups, allDropoffs);

            // 4. Cost Check (Example: merged route shouldn't be > 30% longer than sum of individual)
            //    TODO: Refine cost check - compare against existing route + estimated new route cost
            double existingDistance = existingRoute.getTotalDistanceKm();
            double newRouteDistance = newPassengerRoute.getTotalDistanceKm(); // This is just A->B distance
            double mergedDistance = mergedRoute.getTotalDistanceKm();
            double thresholdFactor = 1.30; // Allow 30% increase

            // A simple check - is merged significantly longer than just adding the new ride naively?
            if (mergedDistance > (existingDistance + newRouteDistance) * thresholdFactor) {
                System.out.println("Merge rejected: Merged distance (" + mergedDistance + "km) exceeds threshold over sum of individual (" + (existingDistance + newRouteDistance) + "km)");
                return Optional.empty();
            }
            // Add duration checks if needed

            return Optional.of(mergedRoute);

        } catch (Exception e) {
            System.err.println("Error during tryMergeRoutes -> generateMergedRoute: " + e.getMessage());
            // Don't propagate OR-Tools errors necessarily, just fail the merge
            return Optional.empty();
        }
    }

    // Helper to extract pickups/dropoffs based on metadata
    private void extractPickupsDropoffs(Route route, List<Pair<String, LatLng>> pickups, List<Pair<String, LatLng>> dropoffs) {
        for (Waypoint wp : route.getWaypointsList()) {
            for (WaypointMetadata meta : wp.getWaypointMetadataList()) {
                if (meta.getRiderId() != null && !meta.getRiderId().isEmpty()) {
                    if (meta.getWaypointType() == WaypointType.ORIGIN) {
                        pickups.add(Pair.of(meta.getRiderId(), wp.getLocation()));
                    } else if (meta.getWaypointType() == WaypointType.DESTINATION) {
                        dropoffs.add(Pair.of(meta.getRiderId(), wp.getLocation()));
                    }
                }
            }
        }
    }


    // --- getRouteSegment (already provided and looks reasonable) ---
    @Override
    public Route getRouteSegment(Route fullRoute, LatLng startLocation, LatLng destinationLocation) {
        // ... (Implementation from previous step) ...
        if (fullRoute == null || fullRoute.getWaypointsCount() < 2 || startLocation == null || destinationLocation == null) {
            System.err.println("getRouteSegment: Invalid input, returning full route.");
            return fullRoute;
        }

        List<Waypoint> waypoints = fullRoute.getWaypointsList();
        int startIndex = findClosestWaypointIndex(waypoints, startLocation); // Use helper
        int endIndex = findClosestWaypointIndex(waypoints, destinationLocation); // Use helper

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            System.err.println("getRouteSegment: Indices invalid or out of order. Start: " + startIndex + ", End: " + endIndex + ". Returning full route.");
            return fullRoute;
        }

        if (startIndex == endIndex) { // Handle case where start and end match the same waypoint
            if (startIndex < waypoints.size()) {
                return Route.newBuilder()
                        .addWaypoints(waypoints.get(startIndex).toBuilder()
                                .setDistanceFromStartKm(0)
                                .setDurationFromStartMin(0)
                                .build())
                        .setTotalDistanceKm(0)
                        .setTotalDurationMin(0)
                        .build();
            } else {
                return Route.newBuilder().build(); // Should not happen if indices are valid
            }
        }


        Route.Builder segmentBuilder = Route.newBuilder();
        double segmentDistance = 0;
        double segmentDuration = 0;
        double distanceAtSegmentStart = waypoints.get(startIndex).getDistanceFromStartKm();
        double durationAtSegmentStart = waypoints.get(startIndex).getDurationFromStartMin();

        for (int i = startIndex; i <= endIndex; i++) {
            Waypoint originalWp = waypoints.get(i);
            Waypoint.Builder segmentWpBuilder = originalWp.toBuilder(); // Keep original metadata

            double relativeDistance = Math.max(0, originalWp.getDistanceFromStartKm() - distanceAtSegmentStart);
            double relativeDuration = Math.max(0, originalWp.getDurationFromStartMin() - durationAtSegmentStart);

            segmentWpBuilder.setDistanceFromStartKm(relativeDistance);
            segmentWpBuilder.setDurationFromStartMin(relativeDuration);

            segmentBuilder.addWaypoints(segmentWpBuilder.build());

            if (i == endIndex) {
                segmentDistance = relativeDistance;
                segmentDuration = relativeDuration;
            }
        }
        segmentBuilder.setTotalDistanceKm(segmentDistance);
        segmentBuilder.setTotalDurationMin(segmentDuration);
        // System.out.println("Generated segment from index " + startIndex + " to " + endIndex);
        return segmentBuilder.build();
    }

    // Helper for finding closest index (needed by getRouteSegment)
    private int findClosestWaypointIndex(List<Waypoint> waypoints, LatLng target) {
        if (waypoints.isEmpty() || target == null) return -1;
        double bestDistSq = Double.MAX_VALUE;
        int bestIndex = -1;
        double targetLat = target.getLatitude();
        double targetLon = target.getLongitude();

        for (int i = 0; i < waypoints.size(); i++) {
            LatLng wpLoc = waypoints.get(i).getLocation();
            // Use squared Euclidean distance for faster comparison (avoid sqrt)
            double distSq = Math.pow(wpLoc.getLatitude() - targetLat, 2) + Math.pow(wpLoc.getLongitude() - targetLon, 2);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestIndex = i;
            }
        }
        // Optional: Add a threshold check - if bestDistSq is too large, maybe return -1
        // double maxAllowedDistSq = Math.pow(0.01, 2); // Approx 1km in degrees^2 (very rough)
        // if (bestDistSq > maxAllowedDistSq) return -1;

        return bestIndex;
    }


    // --- calculateDistance (already provided) ---
    private double calculateDistance(LatLng a, LatLng b) {
        // ... (Haversine formula implementation) ...
        final double R = 6371e3; // Earth's radius in meters
        if (a == null || b == null) return Double.MAX_VALUE;
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

    // --- locationsApproxEqual (already provided) ---
    private boolean locationsApproxEqual(LatLng loc1, LatLng loc2, double tolerance) {
        if (loc1 == null || loc2 == null) return false;
        return Math.abs(loc1.getLatitude() - loc2.getLatitude()) < tolerance &&
                Math.abs(loc1.getLongitude() - loc2.getLongitude()) < tolerance;
    }

} // End of OptimizedRouteService
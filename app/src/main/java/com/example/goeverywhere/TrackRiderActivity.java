package com.example.goeverywhere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.type.LatLngOrBuilder;

import org.example.goeverywhere.protocol.grpc.DriverEnRoute;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.RiderServiceGrpc;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

@AndroidEntryPoint
public class TrackRiderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackRiderActivity";
    
    private TextView driverNameText;
    private TextView etaText;
    private Button backButton;
    private Button refreshButton;
    private GoogleMap mMap;
    
    // Driver tracking data
    private LatLng driverLocation;
    private LatLng destinationLocation;
    private String driverName = "Your Driver";
    private int etaMinutes = 10;
    
    // Ride details
    private String originAddress = "";
    private String destinationAddress = "";
    private double originLat = 0;
    private double originLng = 0;
    private double destinationLat = 0;
    private double destinationLng = 0;
    
    @Inject
    AtomicReference<LoginResponse> sessionHolder;
    
    @Inject
    ManagedChannel managedChannel;
    
    // Ride ID - will be retrieved from SharedPreferences if not passed in intent
    private String rideId;
    private StreamObserver<RiderEvent> rideEventObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_rider);

        // Check if user is logged in
        if (sessionHolder.get() == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        driverNameText = findViewById(R.id.driver_name);
        etaText = findViewById(R.id.eta_text);
        backButton = findViewById(R.id.btn_back);
        refreshButton = findViewById(R.id.btn_refresh);

        // Get ride ID and location information
        loadRideDetailsFromSharedPreferences();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());
        refreshButton.setOnClickListener(v -> refreshDriverLocation());

        // Connect to ride tracking service
        subscribeToRideEvents();
    }
    
    private void loadRideDetailsFromSharedPreferences() {
        // Get ride ID from intent if available, otherwise use from SharedPreferences
        rideId = getIntent().getStringExtra("ride_id");
        
        // Get detailed ride information from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
        SharedPreferences rideHistoryPrefs = getSharedPreferences("ride_history", MODE_PRIVATE);
        
        if (rideId == null || rideId.isEmpty()) {
            // Get from SharedPreferences as fallback
            rideId = prefs.getString("rider_id", null);
            
            if (rideId == null || rideId.isEmpty()) {
                Log.w(TAG, "No ride ID found, using session ID as fallback");
                rideId = sessionHolder.get().getSessionId();
            }
        }
        
        // Get ride origin and destination details
        originAddress = prefs.getString("origin", "Current Location");
        destinationAddress = prefs.getString("destination", "Destination");
        
        // Try to get coordinates - first from driver_requests
        String destLatStr = prefs.getString("destination_lat", null);
        String destLngStr = prefs.getString("destination_lng", null);
        
        if (destLatStr != null && destLngStr != null) {
            try {
                destinationLat = Double.parseDouble(destLatStr);
                destinationLng = Double.parseDouble(destLngStr);
                destinationLocation = new LatLng(destinationLat, destinationLng);
                Log.d(TAG, "Loaded destination from driver_requests: " + destinationLat + ", " + destinationLng);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing destination coordinates", e);
            }
        } else {
            // Try ride_history as fallback
            destinationAddress = rideHistoryPrefs.getString("last_destination", destinationAddress);
        }
        
        // If we still don't have coordinates, use a default
        if (destinationLocation == null) {
            // Default to Downtown Miami
            destinationLocation = new LatLng(25.7839, -80.2102);
            destinationLat = destinationLocation.latitude;
            destinationLng = destinationLocation.longitude;
            Log.d(TAG, "Using default destination location");
        }
        
        // Log the loaded details
        Log.d(TAG, "Loaded ride details - ID: " + rideId + 
                ", Origin: " + originAddress + 
                ", Destination: " + destinationAddress);
    }

    private void subscribeToRideEvents() {
        // Use a non-blocking async stub to subscribe to ride events
        RiderServiceGrpc.RiderServiceStub riderService = RiderServiceGrpc.newStub(managedChannel);
        
        // Create the ride request with our session ID and location information
        RideRequest.Builder requestBuilder = RideRequest.newBuilder()
                .setSessionId(sessionHolder.get().getSessionId());
        
        // Add origin information
        if (!originAddress.isEmpty()) {
            requestBuilder.setOrigin(originAddress);
        }
        
        // Add destination information
        if (!destinationAddress.isEmpty()) {
            requestBuilder.setDestination(destinationAddress);
        }
        
        // Set a scheduled pickup time if needed (0 means immediate)
        // requestBuilder.setScheduledPickupTime(0);
        
        RideRequest request = requestBuilder.build();
        
        // Log the request details for debugging
        Log.d(TAG, "Sending ride request: SessionID=" + request.getSessionId() + 
                ", Origin=" + request.getOrigin() + 
                ", Destination=" + request.getDestination());
        
        // Create a stream observer to handle incoming ride events
        rideEventObserver = new StreamObserver<RiderEvent>() {
            @Override
            public void onNext(RiderEvent riderEvent) {
                // Handle the ride event based on its type
                if (riderEvent.hasDriverEnRoute()) {
                    // Driver location update received
                    handleDriverEnRouteEvent(riderEvent.getDriverEnRoute());
                } else if (riderEvent.hasRideAccepted()) {
                    // Ride was accepted
                    Toast.makeText(TrackRiderActivity.this, 
                            "Driver accepted your ride!", Toast.LENGTH_SHORT).show();
                } else if (riderEvent.hasDriverArrived()) {
                    // Driver arrived at pickup
                    Toast.makeText(TrackRiderActivity.this, 
                            "Driver has arrived!", Toast.LENGTH_LONG).show();
                } else if (riderEvent.hasRideStarted()) {
                    // Ride has started (user picked up)
                    Toast.makeText(TrackRiderActivity.this, 
                            "Your ride has started", Toast.LENGTH_SHORT).show();
                } else if (riderEvent.hasRideCompleted()) {
                    // Ride completed
                    Toast.makeText(TrackRiderActivity.this, 
                            "Your ride is complete! Fare: $" + 
                            riderEvent.getRideCompleted().getFare(), 
                            Toast.LENGTH_LONG).show();
                    finish(); // Close the tracking activity when ride is done
                }
            }

            @Override
            public void onError(Throwable t) {
                // Handle errors with the subscription
                Log.e(TAG, "Error in ride event subscription: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(TrackRiderActivity.this, 
                            "Lost connection to tracking service: " + t.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    // Fall back to simulated data if we lose connection
                    startFallbackSimulation();
                });
            }

            @Override
            public void onCompleted() {
                // Server closed the connection normally
                Log.i(TAG, "Ride event subscription completed");
                runOnUiThread(() -> {
                    Toast.makeText(TrackRiderActivity.this, 
                            "Tracking service disconnected", Toast.LENGTH_SHORT).show();
                });
            }
        };
        
        // Subscribe to ride events with the correct method - 'requestRide'
        riderService.requestRide(request, rideEventObserver);
        Log.d(TAG, "Subscribed to ride events with session ID: " + sessionHolder.get().getSessionId());
    }

    private void handleDriverEnRouteEvent(DriverEnRoute driverEnRoute) {
        // Process the driver's location update
        if (driverEnRoute == null || !driverEnRoute.hasLocation()) return;
        
        com.google.type.LatLng locationProto = driverEnRoute.getLocation();
        
        // Convert the proto LatLng to Google Maps LatLng
        final LatLng newLocation = new LatLng(
                locationProto.getLatitude(),
                locationProto.getLongitude()
        );
        
        // Update UI on the main thread
        runOnUiThread(() -> {
            driverLocation = newLocation;
            updateDriverInfo();
            
            // Update ETA based on distance (simple calculation)
            if (destinationLocation != null) {
                double distance = calculateDistance(driverLocation, destinationLocation);
                // Rough estimate: 30km/h average speed = 0.5km per minute
                etaMinutes = (int) Math.max(1, Math.round(distance / 0.5));
                etaText.setText("ETA: " + etaMinutes + " mins");
            }
        });
    }
    
    // Calculate distance between two points in kilometers
    private double calculateDistance(LatLng start, LatLng end) {
        if (start == null || end == null) return 0;
        
        double earthRadius = 6371; // kilometers
        double dLat = Math.toRadians(end.latitude - start.latitude);
        double dLng = Math.toRadians(end.longitude - start.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void startFallbackSimulation() {
        Log.d(TAG, "Starting fallback simulation since server tracking unavailable");
        // Create default values if we don't have them
        if (driverLocation == null) {
            driverLocation = new LatLng(25.7617, -80.1918); // Miami coordinates
        }
        if (destinationLocation == null) {
            destinationLocation = new LatLng(25.7839, -80.2102); // Downtown Miami
        }
        
        // Notify the user we're using simulated data
        Toast.makeText(this, "Using simulated driver location", Toast.LENGTH_SHORT).show();
        
        // Update the UI with what we have
        updateDriverInfo();
    }

    private void updateDriverInfo() {
        // Update UI
        driverNameText.setText(driverName);
        etaText.setText("ETA: " + etaMinutes + " mins");
        
        // Update map
        if (mMap != null) {
            mMap.clear();
            
            // Add driver marker if we have driver location
            if (driverLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(driverLocation)
                        .title("Driver's Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                
                // Move camera to show driver - this ensures the driver is always in view
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));
            }
            
            // Add destination marker if we have it
            if (destinationLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(destinationLocation)
                        .title("Your Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    private void refreshDriverLocation() {
        Toast.makeText(this, "Refreshing driver location...", Toast.LENGTH_SHORT).show();
        
        // Just update the UI with latest data we have
        // The real updates come from the streaming subscription
        updateDriverInfo();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(TrackRiderActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Apply default map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        // If we already have driver location data, show it
        if (driverLocation != null) {
            updateDriverInfo();
        } else {
            // Just center the map on the destination until we get driver data
            if (destinationLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
                
                // Add destination marker
                mMap.addMarker(new MarkerOptions()
                        .position(destinationLocation)
                        .title("Your Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                
                // Add a toast to let the user know we're waiting for driver location
                Toast.makeText(this, "Waiting for driver location updates...", Toast.LENGTH_SHORT).show();
            } else {
                // Fallback to default location
                LatLng defaultLocation = new LatLng(25.7617, -80.1918); // Miami
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up the event observer
        if (rideEventObserver != null) {
            try {
                // Cancel the subscription stream
                rideEventObserver.onCompleted();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up ride event observer", e);
            }
        }
    }
} 
package com.example.goeverywhere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.goeverywhere.protocol.grpc.AcceptRideRequest;
import org.example.goeverywhere.protocol.grpc.DriverArrivedRequest;
import org.example.goeverywhere.protocol.grpc.DriverServiceGrpc;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.RejectRideRequest;
import org.example.goeverywhere.protocol.grpc.RideCompletedRequest;
import org.example.goeverywhere.protocol.grpc.RideStartedRequest;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class DriverRideDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Inject
    ManagedChannel managedChannel;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    private GoogleMap mMap;
    private String riderId;
    private boolean isAccepted;
    private boolean isRideStarted = false;
    
    private TextView riderInfoTextView;
    private TextView pickupLocationTextView;
    private TextView dropoffLocationTextView;
    private TextView rideStatusTextView;
    private Button primaryActionButton;
    private Button secondaryActionButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride_detail);

        // Get ride info from intent first to avoid NPE if no riderId
        riderId = getIntent().getStringExtra("rider_id");
        isAccepted = getIntent().getBooleanExtra("is_accepted", false);

        if (riderId == null) {
            Toast.makeText(this, "No ride information provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verify driver is logged in - but don't redirect if session exists 
        // even with wrong type (we'll handle this better below)
        if (sessionHolder.get() == null) {
            Toast.makeText(this, "Session expired. Please log in again", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        riderInfoTextView = findViewById(R.id.rider_info_text);
        pickupLocationTextView = findViewById(R.id.pickup_location_text);
        dropoffLocationTextView = findViewById(R.id.dropoff_location_text);
        rideStatusTextView = findViewById(R.id.ride_status_text);
        primaryActionButton = findViewById(R.id.primary_action_button);
        secondaryActionButton = findViewById(R.id.secondary_action_button);
        backButton = findViewById(R.id.back_button);

        // Get ride details from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
        String savedRiderId = prefs.getString("rider_id", null);
        
        // Display warning if user is not a driver but allow them to view the details anyway
        if (sessionHolder.get().getUserType() != UserType.DRIVER) {
            Toast.makeText(this, "Note: You are not logged in as a driver", Toast.LENGTH_LONG).show();
        }
        
        // Only load the details if they match the riderId we're displaying
        if (savedRiderId != null && savedRiderId.equals(riderId)) {
            String origin = prefs.getString("origin", "Current Location");
            String destination = prefs.getString("destination", null);
            String destinationLat = prefs.getString("destination_lat", null);
            String destinationLng = prefs.getString("destination_lng", null);
            String passengers = prefs.getString("passengers", "1");
            String dateTime = prefs.getString("dateTime", null);
            boolean isScheduled = prefs.getBoolean("is_scheduled", false);
            
            // Set rider info with more details
            riderInfoTextView.setText("Rider ID: " + riderId + 
                    (passengers != null ? "\nPassengers: " + passengers : "") +
                    (isScheduled ? "\nScheduled Ride" : "\nImmediate Pickup"));
            
            // Set pickup and dropoff locations
            pickupLocationTextView.setText("Pickup: " + origin);
            
            if (destination != null) {
                dropoffLocationTextView.setText("Dropoff: " + destination);
            } else if (destinationLat != null && destinationLng != null) {
                // Try to reverse geocode the coordinates for a better address
                new android.os.Handler().post(() -> reverseGeocode(
                        Double.parseDouble(destinationLat), 
                        Double.parseDouble(destinationLng), 
                        dropoffLocationTextView));
            } else {
                dropoffLocationTextView.setText("Dropoff: Coconut Creek, FL");
            }
        } else {
            // Fallback to defaults
            riderInfoTextView.setText("Rider ID: " + riderId);
            pickupLocationTextView.setText("Pickup: Current Location");
            dropoffLocationTextView.setText("Dropoff: Destination");
        }
        
        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up UI based on ride state
        updateUI();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error: Map fragment not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (!isAccepted) {
            // Ride not yet accepted
            rideStatusTextView.setText("Status: Pending");
            primaryActionButton.setText("Accept Ride");
            secondaryActionButton.setText("Reject Ride");
            
            primaryActionButton.setOnClickListener(v -> acceptRide());
            secondaryActionButton.setOnClickListener(v -> rejectRide());
        } else if (!isRideStarted) {
            // Ride accepted but not started
            rideStatusTextView.setText("Status: Pickup rider");
            primaryActionButton.setText("Arrived at Pickup");
            secondaryActionButton.setText("Cancel Ride");
            
            primaryActionButton.setOnClickListener(v -> arrivedAtPickup());
            secondaryActionButton.setOnClickListener(v -> cancelRide());
        } else {
            // Ride in progress
            rideStatusTextView.setText("Status: In progress");
            primaryActionButton.setText("Complete Ride");
            secondaryActionButton.setText("Emergency");
            
            primaryActionButton.setOnClickListener(v -> completeRide());
            secondaryActionButton.setOnClickListener(v -> handleEmergency());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Get ride details from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
        String savedRiderId = prefs.getString("rider_id", null);
        
        // Default locations
        LatLng pickup = new LatLng(26.270033371253067, -80.26316008718736); // Example origin
        LatLng dropoff = new LatLng(26.250033371253067, -80.24316008718736); // Example destination
        
        // If we have stored coordinates for this ride, use them
        if (savedRiderId != null && savedRiderId.equals(riderId)) {
            // Try to get destination coordinates
            String destinationLat = prefs.getString("destination_lat", null);
            String destinationLng = prefs.getString("destination_lng", null);
            
            if (destinationLat != null && destinationLng != null) {
                try {
                    double lat = Double.parseDouble(destinationLat);
                    double lng = Double.parseDouble(destinationLng);
                    dropoff = new LatLng(lat, lng);
                } catch (NumberFormatException e) {
                    // Use default coordinates if parsing fails
                    Toast.makeText(this, "Error parsing coordinates, using defaults", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        // Add markers
        mMap.addMarker(new MarkerOptions().position(pickup).title("Pickup Location"));
        mMap.addMarker(new MarkerOptions().position(dropoff).title("Dropoff Location"));
        
        // Move camera to show both markers
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 13));
        
        // Draw route between points
        drawRoute(pickup, dropoff);
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&key=AIzaSyB0EPNYHnZ86S_VWxbB3LxgAKWA4cCYjHQ";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> 
                        Toast.makeText(DriverRideDetailActivity.this, 
                                "Failed to get directions: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> 
                            Toast.makeText(DriverRideDetailActivity.this, 
                                    "Error getting directions", 
                                    Toast.LENGTH_SHORT).show());
                    return;
                }
                
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    JSONArray routes = json.getJSONArray("routes");
                    
                    if (routes.length() == 0) {
                        runOnUiThread(() -> 
                                Toast.makeText(DriverRideDetailActivity.this, 
                                        "No routes found", 
                                        Toast.LENGTH_SHORT).show());
                        return;
                    }
                    
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedString = overviewPolyline.getString("points");
                    List<LatLng> decodedPoints = decodePolyline(encodedString);
                    
                    runOnUiThread(() -> 
                            mMap.addPolyline(new PolylineOptions()
                                    .addAll(decodedPoints)
                                    .width(10)
                                    .color(Color.BLUE)));
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> 
                            Toast.makeText(DriverRideDetailActivity.this, 
                                    "Error processing directions data: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dlat;
            
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lng += dlng;
            
            poly.add(new LatLng(((double) lat / 1E5), ((double) lng / 1E5)));
        }
        return poly;
    }

    private void acceptRide() {
        try {
            // In a real app with gRPC service:
            // DriverServiceGrpc.DriverServiceBlockingStub driverService = 
            //        DriverServiceGrpc.newBlockingStub(managedChannel);
            // driverService.acceptRide(AcceptRideRequest.newBuilder()
            //        .setSessionId(sessionHolder.get().getSessionId())
            //        .setRiderId(riderId)
            //        .build());
            
            isAccepted = true;
            Toast.makeText(this, "Ride accepted successfully", Toast.LENGTH_SHORT).show();
            
            // Get ride details from SharedPreferences
            android.content.SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
            String destinationLat = prefs.getString("destination_lat", null);
            String destinationLng = prefs.getString("destination_lng", null);
            String destination = prefs.getString("destination", null);
            
            // Navigate to MapsActivity with ride details
            Intent intent = new Intent(DriverRideDetailActivity.this, MapsActivity.class);
            if (destinationLat != null && destinationLng != null) {
                try {
                    double latitude = Double.parseDouble(destinationLat);
                    double longitude = Double.parseDouble(destinationLng);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                } catch (NumberFormatException e) {
                    // If parsing fails, don't add coordinates
                    Toast.makeText(this, "Could not parse coordinates", Toast.LENGTH_SHORT).show();
                }
            }
            
            // Add other ride details if needed
            intent.putExtra("is_driver", true);  // Flag to indicate we're in driver mode
            intent.putExtra("ride_accepted", true);
            intent.putExtra("rider_id", riderId);
            if (destination != null) {
                intent.putExtra("destination_address", destination);
            }
            
            startActivity(intent);
            
            // Don't finish this activity yet, so the driver can return to it
        } catch (Exception e) {
            Toast.makeText(this, "Failed to accept ride: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void rejectRide() {
        try {
            // In a real app with gRPC service:
            // DriverServiceGrpc.DriverServiceBlockingStub driverService = 
            //        DriverServiceGrpc.newBlockingStub(managedChannel);
            // driverService.rejectRide(RejectRideRequest.newBuilder()
            //        .setSessionId(sessionHolder.get().getSessionId())
            //        .setRiderId(riderId)
            //        .build());
            
            Toast.makeText(this, "Ride rejected", Toast.LENGTH_SHORT).show();
            
            // Navigate back to DriverHomeActivity
            Intent intent = new Intent(DriverRideDetailActivity.this, DriverHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to reject ride: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void arrivedAtPickup() {
        DriverServiceGrpc.DriverServiceBlockingStub driverService = 
                DriverServiceGrpc.newBlockingStub(managedChannel);
        
        try {
            driverService.driverArrived(DriverArrivedRequest.newBuilder()
                    .setSessionId(sessionHolder.get().getSessionId())
                    .setRiderId(riderId)
                    .build());
            
            // After arriving, the driver can start the ride
            driverService.rideStarted(RideStartedRequest.newBuilder()
                    .setSessionId(sessionHolder.get().getSessionId())
                    .setRiderId(riderId)
                    .build());
            
            isRideStarted = true;
            Toast.makeText(this, "Ride started", Toast.LENGTH_SHORT).show();
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to update ride status: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void completeRide() {
        DriverServiceGrpc.DriverServiceBlockingStub driverService = 
                DriverServiceGrpc.newBlockingStub(managedChannel);
        
        try {
            driverService.rideCompleted(RideCompletedRequest.newBuilder()
                    .setSessionId(sessionHolder.get().getSessionId())
                    .setRiderId(riderId) // Note: In a real app, need to get actual ride_id
                    .build());
            
            Toast.makeText(this, "Ride completed successfully", Toast.LENGTH_SHORT).show();
            
            // Return to the driver home screen
            Intent intent = new Intent(DriverRideDetailActivity.this, DriverHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to complete ride: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRide() {
        // In a real app, this would call an API to cancel the ride
        Toast.makeText(this, "Ride cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleEmergency() {
        // In a real app, this would show emergency options or contact support
        Toast.makeText(this, "Emergency options would appear here", Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DriverRideDetailActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Helper method to reverse geocode coordinates into address
    private void reverseGeocode(double latitude, double longitude, TextView textView) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
            java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                String addressStr = "";
                
                // Build address string
                if (address.getMaxAddressLineIndex() > 0) {
                    addressStr = address.getAddressLine(0);
                } else {
                    // Build from parts if no complete address line
                    if (address.getThoroughfare() != null) {
                        addressStr += address.getThoroughfare() + ", ";
                    }
                    if (address.getLocality() != null) {
                        addressStr += address.getLocality() + ", ";
                    }
                    if (address.getAdminArea() != null) {
                        addressStr += address.getAdminArea();
                    }
                }
                
                if (!addressStr.isEmpty()) {
                    final String finalAddress = addressStr;
                    runOnUiThread(() -> textView.setText("Dropoff: " + finalAddress));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
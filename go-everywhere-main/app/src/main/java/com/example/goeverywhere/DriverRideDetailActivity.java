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

        // Verify driver is logged in
        if (sessionHolder.get() == null || sessionHolder.get().getUserType() != UserType.DRIVER) {
            Toast.makeText(this, "Please log in as a driver to continue", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Get ride info from intent
        riderId = getIntent().getStringExtra("rider_id");
        isAccepted = getIntent().getBooleanExtra("is_accepted", false);

        if (riderId == null) {
            Toast.makeText(this, "No ride information provided", Toast.LENGTH_SHORT).show();
            finish();
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

        // Set rider info
        riderInfoTextView.setText("Rider ID: " + riderId);
        pickupLocationTextView.setText("Pickup: North Broward, FL");
        dropoffLocationTextView.setText("Dropoff: Coconut Creek, FL");
        
        // Set back button listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up UI based on ride state
        updateUI();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Set example locations for demo purposes
        LatLng pickup = new LatLng(26.270033371253067, -80.26316008718736); // Example origin
        LatLng dropoff = new LatLng(26.250033371253067, -80.24316008718736); // Example destination
        
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
        DriverServiceGrpc.DriverServiceBlockingStub driverService = 
                DriverServiceGrpc.newBlockingStub(managedChannel);
        
        try {
            driverService.acceptRide(AcceptRideRequest.newBuilder()
                    .setSessionId(sessionHolder.get().getSessionId())
                    .setRiderId(riderId)
                    .build());
            
            isAccepted = true;
            Toast.makeText(this, "Ride accepted successfully", Toast.LENGTH_SHORT).show();
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to accept ride: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void rejectRide() {
        DriverServiceGrpc.DriverServiceBlockingStub driverService = 
                DriverServiceGrpc.newBlockingStub(managedChannel);
        
        try {
            driverService.rejectRide(RejectRideRequest.newBuilder()
                    .setSessionId(sessionHolder.get().getSessionId())
                    .setRiderId(riderId)
                    .build());
            
            Toast.makeText(this, "Ride rejected", Toast.LENGTH_SHORT).show();
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
                    .setRideId(riderId) // Note: In a real app, need to get actual ride_id
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
} 
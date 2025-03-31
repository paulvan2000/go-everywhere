package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.example.goeverywhere.protocol.grpc.DriverEvent;
import org.example.goeverywhere.protocol.grpc.DriverServiceGrpc;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.RideRequested;
import org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest;
import org.example.goeverywhere.protocol.grpc.UserType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class DriverRideRequestsActivity extends AppCompatActivity {

    @Inject
    ManagedChannel managedChannel;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    private RecyclerView recyclerView;
    private RideRequestAdapter adapter;
    private List<RideRequested> rideRequests = new ArrayList<>();
    private ImageButton backButton;
    private TextView noRequestsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride_requests);

        // Verify driver is logged in
        if (sessionHolder.get() == null || sessionHolder.get().getUserType() != UserType.DRIVER) {
            Toast.makeText(this, "Please log in as a driver to continue", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        backButton = findViewById(R.id.back_button);
        recyclerView = findViewById(R.id.ride_requests_recycler_view);
        noRequestsTextView = findViewById(R.id.no_requests_text);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideRequestAdapter(rideRequests);
        recyclerView.setAdapter(adapter);
        
        // Set back button listener
        backButton.setOnClickListener(v -> finish());
        
        // Check for saved ride requests from riders
        checkForLocalRideRequests();
        
        // Subscribe for ride events (ride requests)
        subscribeForRideEvents();
    }

    // Check SharedPreferences for any ride requests that riders have created
    private void checkForLocalRideRequests() {
        android.content.SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
        
        // Check if there's a saved request
        String riderId = prefs.getString("rider_id", null);
        
        if (riderId != null) {
            // Create a ride request from saved data
            RideRequested.Builder builder = RideRequested.newBuilder()
                    .setRiderId(riderId);
            
            // Add to the list
            rideRequests.add(builder.build());
            adapter.notifyItemInserted(rideRequests.size() - 1);
            
            // Hide "No ride requests" text
            noRequestsTextView.setVisibility(View.GONE);
            
            Toast.makeText(this, "Found ride request from rider: " + riderId, 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void subscribeForRideEvents() {
        DriverServiceGrpc.DriverServiceStub driverService = DriverServiceGrpc.newStub(managedChannel);
        
        driverService.subscribeForRideEvents(
                SubscribeForRideEventsRequest.newBuilder()
                        .setSessionId(sessionHolder.get().getSessionId())
                        .build(),
                new StreamObserver<DriverEvent>() {
                    @Override
                    public void onNext(DriverEvent driverEvent) {
                        if (driverEvent.getEventCase() == DriverEvent.EventCase.RIDE_REQUESTED) {
                            RideRequested rideRequest = driverEvent.getRideRequested();
                            runOnUiThread(() -> {
                                // Add new ride request to the list
                                rideRequests.add(rideRequest);
                                adapter.notifyItemInserted(rideRequests.size() - 1);
                                
                                // Hide "No ride requests" text if there are ride requests
                                if (rideRequests.size() > 0) {
                                    noRequestsTextView.setVisibility(View.GONE);
                                }
                                
                                Toast.makeText(DriverRideRequestsActivity.this, 
                                        "New ride request received", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(() -> {
                            Toast.makeText(DriverRideRequestsActivity.this, 
                                    "Error receiving ride requests: " + throwable.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onCompleted() {
                        // This would be called when the server completes the stream, but for subscriptions
                        // this typically doesn't happen unless there's an error or the connection is closed
                    }
                });

        // For testing purposes, if we have no requests after a moment, show a dummy entry
        if (rideRequests.isEmpty()) {
            // Simulating a ride request for testing
            // In a real app, this would come from the server
            recyclerView.postDelayed(() -> {
                if (rideRequests.isEmpty()) {
                    // Add a dummy ride request for testing UI
                    rideRequests.add(createDummyRideRequest());
                    adapter.notifyItemInserted(0);
                    noRequestsTextView.setVisibility(View.GONE);
                }
            }, 2000); // 2 seconds delay
        }
    }

    private RideRequested createDummyRideRequest() {
        // Create a dummy ride request for testing
        return RideRequested.newBuilder()
                .setRiderId("test_rider_123")
                .build();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DriverRideRequestsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // ViewHolder for ride request items
    private class RideRequestViewHolder extends RecyclerView.ViewHolder {
        TextView riderIdTextView;
        TextView pickupLocationTextView;
        TextView dropoffLocationTextView;
        Button viewDetailsButton;
        Button acceptButton;
        Button rejectButton;

        RideRequestViewHolder(View itemView) {
            super(itemView);
            riderIdTextView = itemView.findViewById(R.id.rider_id_text);
            pickupLocationTextView = itemView.findViewById(R.id.pickup_location_text);
            dropoffLocationTextView = itemView.findViewById(R.id.dropoff_location_text);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }

    // Adapter for ride requests RecyclerView
    private class RideRequestAdapter extends RecyclerView.Adapter<RideRequestViewHolder> {
        private List<RideRequested> requestsList;

        RideRequestAdapter(List<RideRequested> requestsList) {
            this.requestsList = requestsList;
        }

        @NonNull
        @Override
        public RideRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ride_request, parent, false);
            return new RideRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RideRequestViewHolder holder, int position) {
            RideRequested request = requestsList.get(position);
            
            // Set ride request details
            holder.riderIdTextView.setText("Rider ID: " + request.getRiderId());
            
            // Get actual locations from SharedPreferences
            android.content.SharedPreferences prefs = holder.itemView.getContext()
                    .getSharedPreferences("driver_requests", android.content.Context.MODE_PRIVATE);
            
            // Only check if the rider ID matches
            if (prefs.getString("rider_id", "").equals(request.getRiderId())) {
                // Get location info
                String origin = prefs.getString("origin", "Current Location");
                String destination = prefs.getString("destination", null);
                String destinationLat = prefs.getString("destination_lat", null);
                String destinationLng = prefs.getString("destination_lng", null);
                
                // Set pickup location
                holder.pickupLocationTextView.setText("Pickup: " + origin);
                
                // Set dropoff location
                if (destination != null) {
                    holder.dropoffLocationTextView.setText("Dropoff: " + destination);
                } else if (destinationLat != null && destinationLng != null) {
                    holder.dropoffLocationTextView.setText("Dropoff: Processing...");
                    
                    // Try to reverse geocode the coordinates on a background thread
                    new android.os.Handler().post(() -> {
                        try {
                            double lat = Double.parseDouble(destinationLat);
                            double lng = Double.parseDouble(destinationLng);
                            
                            android.location.Geocoder geocoder = new android.location.Geocoder(
                                    holder.itemView.getContext(), java.util.Locale.getDefault());
                            java.util.List<android.location.Address> addresses = 
                                    geocoder.getFromLocation(lat, lng, 1);
                            
                            if (addresses != null && !addresses.isEmpty()) {
                                android.location.Address address = addresses.get(0);
                                String addressStr = "";
                                
                                // Get the complete address line if available
                                if (address.getMaxAddressLineIndex() >= 0) {
                                    addressStr = address.getAddressLine(0);
                                } else {
                                    // Build from parts
                                    if (address.getThoroughfare() != null) {
                                        addressStr += address.getThoroughfare();
                                    }
                                    if (address.getLocality() != null) {
                                        if (!addressStr.isEmpty()) addressStr += ", ";
                                        addressStr += address.getLocality();
                                    }
                                    if (address.getAdminArea() != null) {
                                        if (!addressStr.isEmpty()) addressStr += ", ";
                                        addressStr += address.getAdminArea();
                                    }
                                }
                                
                                if (!addressStr.isEmpty()) {
                                    final String finalAddress = addressStr;
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                            holder.dropoffLocationTextView.setText("Dropoff: " + finalAddress));
                                } else {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                            holder.dropoffLocationTextView.setText("Dropoff: " + lat + ", " + lng));
                                }
                            } else {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                        holder.dropoffLocationTextView.setText("Dropoff: " + lat + ", " + lng));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                    holder.dropoffLocationTextView.setText("Dropoff: Location unavailable"));
                        }
                    });
                } else {
                    holder.dropoffLocationTextView.setText("Dropoff: Location not specified");
                }
            } else {
                // Default values if no matching ride request found
                holder.pickupLocationTextView.setText("Pickup: Current Location");
                holder.dropoffLocationTextView.setText("Dropoff: Destination");
            }
            
            // Set button click listeners
            holder.viewDetailsButton.setOnClickListener(v -> {
                // Navigate to ride details page
                Intent intent = new Intent(v.getContext(), DriverRideDetailActivity.class);
                intent.putExtra("rider_id", request.getRiderId());
                v.getContext().startActivity(intent);
            });
            
            holder.acceptButton.setOnClickListener(v -> {
                // Accept the ride
                acceptRide(request.getRiderId(), position);
            });
            
            holder.rejectButton.setOnClickListener(v -> {
                // Reject the ride
                rejectRide(request.getRiderId(), position);
            });
        }

        @Override
        public int getItemCount() {
            return requestsList.size();
        }
    }

    private void acceptRide(String riderId, int position) {
        // In a real app, this would call the gRPC service to accept a ride
        Toast.makeText(this, "Accepting ride from " + riderId, Toast.LENGTH_SHORT).show();
        
        // Get ride details from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("driver_requests", MODE_PRIVATE);
        String destinationLat = prefs.getString("destination_lat", null);
        String destinationLng = prefs.getString("destination_lng", null);
        
        // Navigate directly to the MapsActivity to show the route
        Intent intent = new Intent(DriverRideRequestsActivity.this, MapsActivity.class);
        intent.putExtra("rider_id", riderId);
        intent.putExtra("is_driver", true);
        intent.putExtra("ride_accepted", true);
        
        // Add coordinates if available
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
        
        startActivity(intent);
        
        // Remove from list (in a real app, we'd wait for server confirmation)
        rideRequests.remove(position);
        adapter.notifyItemRemoved(position);
        
        if (rideRequests.isEmpty()) {
            noRequestsTextView.setVisibility(View.VISIBLE);
        }
    }

    private void rejectRide(String riderId, int position) {
        // In a real app, this would call the gRPC service to reject a ride
        Toast.makeText(this, "Rejecting ride from " + riderId, Toast.LENGTH_SHORT).show();
        
        // Navigate back to DriverHomeActivity
        Intent intent = new Intent(DriverRideRequestsActivity.this, DriverHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        // Remove from list (in a real app, we'd wait for server confirmation)
        rideRequests.remove(position);
        adapter.notifyItemRemoved(position);
        
        if (rideRequests.isEmpty()) {
            noRequestsTextView.setVisibility(View.VISIBLE);
        }
    }
} 
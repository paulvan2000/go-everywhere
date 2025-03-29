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
        
        // Subscribe for ride events (ride requests)
        subscribeForRideEvents();
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
            
            // In a real app, these would come from the ride request object
            holder.pickupLocationTextView.setText("Pickup: North Broward, FL");
            holder.dropoffLocationTextView.setText("Dropoff: Coconut Creek, FL");
            
            // Set button click listeners
            holder.viewDetailsButton.setOnClickListener(v -> {
                // Navigate to ride details page
                Intent intent = new Intent(DriverRideRequestsActivity.this, DriverRideDetailActivity.class);
                intent.putExtra("rider_id", request.getRiderId());
                startActivity(intent);
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
        
        // Navigate to the ride detail activity
        Intent intent = new Intent(DriverRideRequestsActivity.this, DriverRideDetailActivity.class);
        intent.putExtra("rider_id", riderId);
        intent.putExtra("is_accepted", true);
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
        
        // Remove from list (in a real app, we'd wait for server confirmation)
        rideRequests.remove(position);
        adapter.notifyItemRemoved(position);
        
        if (rideRequests.isEmpty()) {
            noRequestsTextView.setVisibility(View.VISIBLE);
        }
    }
} 
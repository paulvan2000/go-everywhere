package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

@AndroidEntryPoint
public class HistoryActivity extends AppCompatActivity {

    private TextView dateDetails;
    private TextView timeDetails;
    private TextView pickupLocation;
    private TextView dropoffLocation;
    private ImageButton profileButton;
    private ImageButton homeButton;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UI elements
        dateDetails = findViewById(R.id.dateDetails);
        timeDetails = findViewById(R.id.timeDetails);
        pickupLocation = findViewById(R.id.pickupLocation);
        dropoffLocation = findViewById(R.id.dropoffLocation);
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);

        // Set up navigation button listeners
        profileButton.setOnClickListener(v -> navigateToProfile());
        homeButton.setOnClickListener(v -> navigateToHome());

        // Load ride history data
        loadRideHistory();
    }

    private void loadRideHistory() {
        // This would typically fetch data from a backend service
        // For demonstration, we'll set some placeholder values
        
        // In a real implementation, you would use gRPC to fetch this data
        // Example: RideHistoryRequest request = RideHistoryRequest.newBuilder()
        //                                          .setUserId(sessionHolder.get().getUserId())
        //                                          .build();
        
        dateDetails.setText("Date: February 15, 2023");
        timeDetails.setText("Time: 10:30 AM - 11:15 AM");
        pickupLocation.setText("Pick-up Location: 123 Main St, Rural County");
        dropoffLocation.setText("Drop-off Location: Rural County Hospital");
    }

    private void navigateToProfile() {
        Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
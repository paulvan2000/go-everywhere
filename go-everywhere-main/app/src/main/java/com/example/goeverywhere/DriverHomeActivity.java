package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.UserType;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class DriverHomeActivity extends AppCompatActivity {

    private Button viewRideRequestsButton;
    private Button driverStatusButton;
    private ImageButton profileButton;
    private TextView welcomeTextView;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    private boolean driverAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        // Verify driver is logged in
        if (sessionHolder.get() == null || sessionHolder.get().getUserType() != UserType.DRIVER) {
            Toast.makeText(this, "Please log in as a driver to continue", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        welcomeTextView = findViewById(R.id.driver_welcome_text);
        viewRideRequestsButton = findViewById(R.id.view_ride_requests_button);
        driverStatusButton = findViewById(R.id.driver_status_button);
        profileButton = findViewById(R.id.driver_profile_button);

        // Set welcome message
        welcomeTextView.setText("Welcome, Driver");

        // Update driver status button text
        updateDriverStatusButtonText();

        // Set click listeners
        viewRideRequestsButton.setOnClickListener(v -> navigateToRideRequests());
        driverStatusButton.setOnClickListener(v -> toggleDriverStatus());
        profileButton.setOnClickListener(v -> navigateToProfile());
    }

    private void navigateToRideRequests() {
        if (!driverAvailable) {
            Toast.makeText(this, "You must be available to view ride requests", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(DriverHomeActivity.this, DriverRideRequestsActivity.class);
        startActivity(intent);
    }

    private void toggleDriverStatus() {
        driverAvailable = !driverAvailable;
        updateDriverStatusButtonText();
        
        String statusMessage = driverAvailable ? 
                "You are now available for ride requests" : 
                "You are now unavailable for ride requests";
        Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
        
        // TODO: Notify server about driver availability status
    }

    private void updateDriverStatusButtonText() {
        driverStatusButton.setText(driverAvailable ? 
                "Go Offline" : 
                "Go Online");
    }

    private void navigateToProfile() {
        Intent intent = new Intent(DriverHomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DriverHomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
} 
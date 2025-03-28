package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private Button bookingButton;
    private Button planningButton;
    private ImageButton profileButton;
    
    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Initialize UI elements
        bookingButton = findViewById(R.id.booking_button);
        planningButton = findViewById(R.id.planning_button);
        profileButton = findViewById(R.id.profile);

        // Set click listeners
        bookingButton.setOnClickListener(v -> navigateToBooking());
        planningButton.setOnClickListener(v -> navigateToPlanTrip());
        profileButton.setOnClickListener(v -> navigateToProfile());
    }

    private void navigateToBooking() {
        // Debug the session state
        if (sessionHolder.get() == null) {
            Toast.makeText(this, "Session is null in HomeActivity!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Session is valid: " + sessionHolder.get().getSessionId().substring(0, 5) + "...", Toast.LENGTH_SHORT).show();
        }
        
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void navigateToPlanTrip() {
        Intent intent = new Intent(HomeActivity.this, PlanActivity.class);
        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
} 
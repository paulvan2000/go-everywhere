package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private Button bookingButton;
    private Button planningButton;
    private ImageButton profileButton;

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
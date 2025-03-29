package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private Button paymentButton;
    private Button historyButton;
    private Button safetyButton;
    private Button notificationsButton;
    private ImageButton profileButton;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        paymentButton = findViewById(R.id.booking_button); // ID is booking_button but used for payment
        historyButton = findViewById(R.id.planning_button); // ID is planning_button but used for history
        safetyButton = findViewById(R.id.safety_button);
        notificationsButton = findViewById(R.id.Notifications_button);
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);

        // Set click listeners
        paymentButton.setOnClickListener(v -> navigateToPayment());
        historyButton.setOnClickListener(v -> navigateToHistory());
        safetyButton.setOnClickListener(v -> navigateToSafety());
        notificationsButton.setOnClickListener(v -> navigateToNotifications());
        homeButton.setOnClickListener(v -> navigateToHome());
        // Profile button doesn't need action as we're already on profile page
    }

    private void navigateToPayment() {
        Intent intent = new Intent(ProfileActivity.this, PaymentActivity.class);
        startActivity(intent);
    }

    private void navigateToHistory() {
        Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    private void navigateToSafety() {
        Intent intent = new Intent(ProfileActivity.this, SafetyActivity.class);
        startActivity(intent);
    }

    private void navigateToNotifications() {
        Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
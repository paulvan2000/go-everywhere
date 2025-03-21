package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileManagementActivity extends AppCompatActivity {
    private Button btnPaymentOptions;
    private Button btnRideHistory;
    private Button btnSafetyAccessibility;
    private Button btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_management);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnPaymentOptions = findViewById(R.id.btnPaymentOptions);
        btnRideHistory = findViewById(R.id.btnRideHistory);
        btnSafetyAccessibility = findViewById(R.id.btnSafetyAccessibility);
        btnNotifications = findViewById(R.id.btnNotifications);
    }

    private void setupClickListeners() {
        btnPaymentOptions.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileManagementActivity.this, PaymentOptionsActivity.class);
            startActivity(intent);
        });

        btnRideHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileManagementActivity.this, RideHistoryActivity.class);
            startActivity(intent);
        });

        btnSafetyAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileManagementActivity.this, SafetyAccessibilityActivity.class);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileManagementActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }
} 
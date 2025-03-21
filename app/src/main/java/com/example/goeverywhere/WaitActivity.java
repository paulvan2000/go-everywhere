package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

@AndroidEntryPoint
public class WaitActivity extends AppCompatActivity {

    private ImageButton profileButton;
    private ImageButton homeButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable waitRunnable;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        // Initialize UI elements
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);

        // Set click listeners
        profileButton.setOnClickListener(v -> navigateToProfile());
        homeButton.setOnClickListener(v -> navigateToHome());

        // Get ride details from the intent
        Intent intent = getIntent();
        String destination = intent.getStringExtra("destination");
        String pickup = intent.getStringExtra("pickup");
        String passengers = intent.getStringExtra("passengers");
        String dateTime = intent.getStringExtra("dateTime");

        // Simulate waiting for a driver
        // In a real app, this would be done through a service or background thread
        // with notifications to the user
        waitRunnable = () -> {
            // After the wait, proceed to the next activity
            Intent coffeeIntent = new Intent(WaitActivity.this, CoffeeActivity.class);
            // Pass ride details to the next activity
            coffeeIntent.putExtra("destination", destination);
            coffeeIntent.putExtra("pickup", pickup);
            coffeeIntent.putExtra("passengers", passengers);
            coffeeIntent.putExtra("dateTime", dateTime);
            startActivity(coffeeIntent);
            finish();
        };

        // Simulate a 5-second wait for demo purposes
        handler.postDelayed(waitRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent leaks
        handler.removeCallbacks(waitRunnable);
    }

    private void navigateToProfile() {
        // Remove the delayed callback to prevent unwanted activity transitions
        handler.removeCallbacks(waitRunnable);
        
        Intent intent = new Intent(WaitActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        // Remove the delayed callback to prevent unwanted activity transitions
        handler.removeCallbacks(waitRunnable);
        
        Intent intent = new Intent(WaitActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
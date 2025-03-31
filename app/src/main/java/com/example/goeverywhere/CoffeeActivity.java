package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoffeeActivity extends AppCompatActivity {

    private ImageButton profileButton;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_coffee);

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

        // In a real app, these details would be used to display more information
        // about the upcoming ride, potentially with a countdown timer or ETA
    }

    private void navigateToProfile() {
        Intent intent = new Intent(CoffeeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(CoffeeActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
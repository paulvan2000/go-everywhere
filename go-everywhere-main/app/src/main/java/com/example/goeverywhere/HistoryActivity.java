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
        // Read ride history from shared preferences
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("ride_history", MODE_PRIVATE);
        
        String destination = sharedPreferences.getString("last_destination", null);
        String pickup = sharedPreferences.getString("last_pickup", null);
        String passengers = sharedPreferences.getString("last_passengers", null);
        String dateTimeStr = sharedPreferences.getString("last_dateTime", null);
        long timestamp = sharedPreferences.getLong("last_timestamp", 0);
        
        if (destination != null && pickup != null) {
            // Format date and time
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.US);
            java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);
            String date = dateTimeStr != null ? dateTimeStr.split(" ")[0] : sdf.format(new java.util.Date(timestamp));
            String time = dateTimeStr != null ? dateTimeStr.split(" ")[1] : timeSdf.format(new java.util.Date(timestamp));
            
            // Update UI with ride history
            dateDetails.setText("Date: " + date);
            timeDetails.setText("Time: " + time);
            pickupLocation.setText("Pick-up Location: " + pickup);
            dropoffLocation.setText("Drop-off Location: " + destination);
        } else {
            // No ride history available, show placeholder data
            dateDetails.setText("Date: No recent trips");
            timeDetails.setText("Time: N/A");
            pickupLocation.setText("Pick-up Location: N/A");
            dropoffLocation.setText("Drop-off Location: N/A");
        }
        
        // In a real app, you would fetch multiple ride history entries from a database
        // and display them in a RecyclerView or similar
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
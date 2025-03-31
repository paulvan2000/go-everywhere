package com.example.goeverywhere;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private EditText inputAddress;
    private Button submitButton;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (sessionHolder.get() == null) {
            Toast.makeText(this, "Session is null in MainActivity!", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        } else {
            Toast.makeText(this, "Session is valid in MainActivity: " + sessionHolder.get().getSessionId().substring(0, 5) + "...", Toast.LENGTH_SHORT).show();
        }

        inputAddress = findViewById(R.id.input_address);
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> {
            String address = inputAddress.getText().toString().trim();
            if (!address.isEmpty()) {
                convertAddressToLatLngAndRedirect(address);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertAddressToLatLngAndRedirect(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                //checking for debugging THIS CODE WONT WORK!!!
                Toast.makeText(this, "Coordinates: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();

                // Save the ride data to history
                saveRideToHistory(address, "Current Location", "1", System.currentTimeMillis());

                // Send ride request to driver
                sendRideRequestToDriver(latitude, longitude);

                // Redirect to WaitActivity instead of MapsActivity
                Intent intent = new Intent(MainActivity.this, WaitActivity.class);
                intent.putExtra("destination", address);
                intent.putExtra("pickup", "Current Location");
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("passengers", "1"); // Default for immediate booking
                intent.putExtra("dateTime", new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)
                        .format(new java.util.Date()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Address not found. Try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to get location. Check your network and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to save ride data to history
    private void saveRideToHistory(String destination, String pickup, String passengers, long timestamp) {
        // In a real implementation, this would store the data in a persistent storage
        // For now, we'll just use SharedPreferences as a simple example
        getSharedPreferences("ride_history", MODE_PRIVATE)
            .edit()
            .putString("last_destination", destination)
            .putString("last_pickup", pickup)
            .putString("last_passengers", passengers)
            .putLong("last_timestamp", timestamp)
            .apply();
    }

    // Helper method to send ride request to driver
    private void sendRideRequestToDriver(double latitude, double longitude) {
        // In a production app, this would call the gRPC service to create a ride request
        // For this example, we'll just simulate it by creating a dummy ride request
        
        // Create a dummy ride request in the shared preferences for the driver to see
        getSharedPreferences("driver_requests", MODE_PRIVATE)
            .edit()
            .putString("rider_id", sessionHolder.get().getSessionId())
            .putString("origin", "Current Location")
            .putString("destination_lat", String.valueOf(latitude))
            .putString("destination_lng", String.valueOf(longitude))
            .putLong("timestamp", System.currentTimeMillis())
            .apply();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

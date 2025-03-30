package com.example.goeverywhere;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

@AndroidEntryPoint
public class PlanActivity extends AppCompatActivity {

    private EditText addressInput;
    private EditText pickupInput;
    private EditText passengerInput;
    private EditText dateTimeInput;
    private Button submitButton;
    private Calendar calendar;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        // Check if user is logged in
        if (sessionHolder.get() == null) {
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        addressInput = findViewById(R.id.input_address);
        pickupInput = findViewById(R.id.input_pickup);
        passengerInput = findViewById(R.id.passenger_number);
        dateTimeInput = findViewById(R.id.date);
        submitButton = findViewById(R.id.submit_button);
        
        // Initialize calendar for date/time picker
        calendar = Calendar.getInstance();
        
        // Set up date/time picker dialog
        dateTimeInput.setInputType(InputType.TYPE_NULL);
        dateTimeInput.setOnClickListener(v -> showDateTimePicker());
        
        // Set up submit button
        submitButton.setOnClickListener(v -> validateAndSubmitPlan());
    }

    private void showDateTimePicker() {
        // Date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                
                                // Format and display the date and time
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                                dateTimeInput.setText(sdf.format(calendar.getTime()));
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void validateAndSubmitPlan() {
        String destination = addressInput.getText().toString().trim();
        String pickup = pickupInput.getText().toString().trim();
        String passengers = passengerInput.getText().toString().trim();
        String dateTime = dateTimeInput.getText().toString().trim();
        
        // Validate inputs
        if (destination.isEmpty() || pickup.isEmpty() || passengers.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int passengerCount = Integer.parseInt(passengers);
            if (passengerCount <= 0 || passengerCount > 8) {
                Toast.makeText(this, "Please enter a valid number of passengers (1-8)", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for passengers", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert addresses to coordinates
        if (!validateAddresses(destination, pickup)) {
            return;
        }
        
        // Save the trip data to history
        saveRideToHistory(destination, pickup, passengers, dateTime);
        
        // Send trip data to drivers
        sendPlannedTripToDrivers(destination, pickup, passengers, dateTime);
        
        // Submit the plan and navigate to CoffeeActivity (instead of wait screen)
        Toast.makeText(this, "Trip planned successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PlanActivity.this, CoffeeActivity.class);
        intent.putExtra("destination", destination);
        intent.putExtra("pickup", pickup);
        intent.putExtra("passengers", passengers);
        intent.putExtra("dateTime", dateTime);
        startActivity(intent);
    }
    
    private boolean validateAddresses(String destination, String pickup) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Validate destination address
            List<Address> destAddressList = geocoder.getFromLocationName(destination, 1);
            if (destAddressList == null || destAddressList.isEmpty()) {
                Toast.makeText(this, "Destination address not found", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            // Validate pickup address
            List<Address> pickupAddressList = geocoder.getFromLocationName(pickup, 1);
            if (pickupAddressList == null || pickupAddressList.isEmpty()) {
                Toast.makeText(this, "Pickup address not found", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to validate addresses. Check your network connection.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Helper method to save ride data to history
    private void saveRideToHistory(String destination, String pickup, String passengers, String dateTime) {
        // In a real implementation, this would store the data in a persistent storage
        // For now, we'll just use SharedPreferences as a simple example
        getSharedPreferences("ride_history", MODE_PRIVATE)
            .edit()
            .putString("last_destination", destination)
            .putString("last_pickup", pickup)
            .putString("last_passengers", passengers)
            .putString("last_dateTime", dateTime)
            .putLong("last_timestamp", System.currentTimeMillis())
            .apply();
    }

    // Helper method to send trip data to drivers
    private void sendPlannedTripToDrivers(String destination, String pickup, String passengers, String dateTime) {
        // In a production app, this would call the gRPC service to create a scheduled ride
        // For this example, we'll just simulate it by creating a dummy ride request
        
        // Create a dummy ride request in the shared preferences for the driver to see
        getSharedPreferences("driver_requests", MODE_PRIVATE)
            .edit()
            .putString("rider_id", sessionHolder.get().getSessionId())
            .putString("origin", pickup)
            .putString("destination", destination)
            .putString("passengers", passengers)
            .putString("dateTime", dateTime)
            .putString("is_scheduled", "true")
            .putLong("timestamp", System.currentTimeMillis())
            .apply();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PlanActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
} 
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
            redirectToLogin();
            return;
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

                //Passes lat and long to MapActivity
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Address not found. Try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to get location. Check your network and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

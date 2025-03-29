package com.example.goeverywhere;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.type.LatLng;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import org.example.goeverywhere.protocol.grpc.*;
import android.Manifest;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Inject
    ManagedChannel managedChannel;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initializing UI Elements
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        // Check if we have an email from signup
        String emailFromSignup = getIntent().getStringExtra("EMAIL");
        if (emailFromSignup != null && !emailFromSignup.isEmpty()) {
            loginEmail.setText(emailFromSignup);
            loginPassword.requestFocus(); // Focus on password field
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        Task<Location> lastLocation = fusedLocationClient.getLastLocation();


        //Login button click
        loginButton.setOnClickListener(view -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            //validating input fields
            if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
                loginEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                loginPassword.setError("Password cannot be empty");
                loginPassword.requestFocus();
                return;
            }

            //authenticating user
            loginUser(email, password, lastLocation);
        });

        //Redirecting to signup selection screen
        signupRedirectText.setOnClickListener(view -> {
            Log.d(TAG, "Signup redirect clicked - going to UserTypeSelectionActivity");
            Intent intent = new Intent(LoginActivity.this, UserTypeSelectionActivity.class);
            startActivity(intent);
            // Keep login screen in back stack
        });
    }

    private void loginUser(String email, String password, Task<Location> lastLocation) {
        UserServiceGrpc.UserServiceBlockingStub userService = UserServiceGrpc.newBlockingStub(managedChannel);
        try {
            Log.d(TAG, "Attempting login for: " + email);
            
            // Clear existing session if any
            sessionHolder.set(null);
            
            // Build login request
            LoginRequest loginRequest = LoginRequest.newBuilder()
                    .setEmail(email)
                    .setPassword(password)
                    .build();
            
            // Execute login request
            LoginResponse loginResponse = userService.login(loginRequest);
                    
            Log.d(TAG, "Login successful");
            Log.d(TAG, "Received user type: " + loginResponse.getUserType() + " (value: " + loginResponse.getUserTypeValue() + ")");
            
            // Save session
            sessionHolder.set(loginResponse);
            
            // Try to update location, but don't let it crash the app if it fails
            try {
                Location result = lastLocation.getResult();
                if (result != null) {
                    userService.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                            .setSessionId(loginResponse.getSessionId())
                            .setLocation(LatLng.newBuilder()
                                    .setLatitude(result.getLatitude())
                                    .setLongitude(result.getLongitude())
                                    .build())
                            .build());
                    Log.d(TAG, "Location updated successfully");
                }
            } catch (Exception locationError) {
                // Log the error but continue with login process
                Log.e(TAG, "Failed to update location: " + locationError.getMessage());
                // Not showing toast to avoid confusing the user
            }
            
            // Redirect based on user type
            Intent intent;
            if (loginResponse.getUserType() == UserType.DRIVER) {
                Log.d(TAG, "User is a driver, redirecting to DriverHomeActivity");
                intent = new Intent(LoginActivity.this, DriverHomeActivity.class);
                Toast.makeText(this, "Logging in as Driver", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "User is a rider, redirecting to HomeActivity");
                intent = new Intent(LoginActivity.this, HomeActivity.class);
                Toast.makeText(this, "Logging in as Rider", Toast.LENGTH_SHORT).show();
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Login failed", e);
            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Just log the error instead of throwing it
        }
    }
}

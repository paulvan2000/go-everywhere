package com.example.goeverywhere;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

        //Redirecting to signup
        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password, Task<Location> lastLocation) {
        UserServiceGrpc.UserServiceBlockingStub userService = UserServiceGrpc.newBlockingStub(managedChannel);
        try {
            Location result = lastLocation.getResult();
            LoginResponse loginResponse = userService.login(LoginRequest.newBuilder()
                    .setEmail(email)
                    .setPassword(password)

                    .build());
            sessionHolder.set(loginResponse);
            userService.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder()
                    .setSessionId(loginResponse.getSessionId())
                    .setLocation(LatLng.newBuilder()
                            .setLatitude(result.getLatitude())
                            .setLongitude(result.getLongitude())
                            .build())
                    .build());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }
}

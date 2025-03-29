package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.SignUpRequest;
import org.example.goeverywhere.protocol.grpc.UserServiceGrpc;
import org.example.goeverywhere.protocol.grpc.UserType;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

import static org.example.goeverywhere.protocol.grpc.UserType.DRIVER;
import static org.example.goeverywhere.protocol.grpc.UserType.RIDER;

@AndroidEntryPoint
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    
    EditText signupName, signupEmail, signupPassword;
    TextView loginRedirectText, userTypeTextView;
    Button signupButton;
    
    private UserType selectedUserType = RIDER; // Default to RIDER if somehow nothing is set

    @Inject
    ManagedChannel managedChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);
        userTypeTextView = findViewById(R.id.user_type_text);
        
        // Get the user type from the intent
        String userTypeString = getIntent().getStringExtra("USER_TYPE");
        if (userTypeString != null) {
            if (userTypeString.equals("DRIVER")) {
                selectedUserType = DRIVER;
                userTypeTextView.setText("Sign Up as Driver");
            } else {
                selectedUserType = RIDER;
                userTypeTextView.setText("Sign Up as Rider");
            }
        }
        
        Log.d(TAG, "Initial setup - Selected user type: " + selectedUserType.name() + " (" + selectedUserType.getNumber() + ")");

        signupButton.setOnClickListener(view -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            
            Log.d(TAG, "Button clicked - User type: " + selectedUserType.name() + " (" + selectedUserType.getNumber() + ")");
            
            // Debug toast to confirm the selected role
            String roleText = (selectedUserType == RIDER) ? "Rider" : "Driver";
            Toast.makeText(SignupActivity.this, "Signing up as: " + roleText, Toast.LENGTH_SHORT).show();
            
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserServiceGrpc.UserServiceBlockingStub userService = UserServiceGrpc.newBlockingStub(managedChannel);
            try {
                // Build the request
                SignUpRequest.Builder requestBuilder = SignUpRequest.newBuilder()
                    .setName(name)
                    .setEmail(email)
                    .setPassword(password);
                
                // Set the user type explicitly
                if (selectedUserType == DRIVER) {
                    Log.d(TAG, "Setting user type to DRIVER (0) explicitly");
                    requestBuilder.setUserType(DRIVER);
                } else {
                    Log.d(TAG, "Setting user type to RIDER (1) explicitly");
                    requestBuilder.setUserType(RIDER);
                }
                
                SignUpRequest request = requestBuilder.build();
                Log.d(TAG, "Final UserType in request: " + request.getUserType() + " (value: " + request.getUserTypeValue() + ")");
                
                userService.signUp(request);
                Log.d(TAG, "Signup successful for: " + email + " as " + request.getUserType());
                
                Toast.makeText(SignupActivity.this, "Signup successful! Please log in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                intent.putExtra("EMAIL", email); // Pass the email to auto-fill in login screen
                startActivity(intent);
                finish();

            } catch (Exception e) {
                Log.e(TAG, "Signup failed", e);
                Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to login screen
        loginRedirectText.setOnClickListener(view -> {
            Log.d(TAG, "Login redirect clicked - going to LoginActivity");
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close signup activity
        });
    }
}
package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserTypeSelectionActivity extends AppCompatActivity {

    private static final String TAG = "UserTypeSelectionActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);
        
        Button btnDriver = findViewById(R.id.btn_driver);
        Button btnRider = findViewById(R.id.btn_rider);
        TextView loginRedirectText = findViewById(R.id.loginRedirectText);
        
        // Driver button click handler
        btnDriver.setOnClickListener(view -> {
            Log.d(TAG, "Driver button clicked - proceeding to signup");
            Intent intent = new Intent(UserTypeSelectionActivity.this, SignupActivity.class);
            intent.putExtra("USER_TYPE", "DRIVER");
            startActivity(intent);
        });
        
        // Rider button click handler
        btnRider.setOnClickListener(view -> {
            Log.d(TAG, "Rider button clicked - proceeding to signup");
            Intent intent = new Intent(UserTypeSelectionActivity.this, SignupActivity.class);
            intent.putExtra("USER_TYPE", "RIDER");
            startActivity(intent);
        });
        
        // Login redirect text click handler - send directly to LoginActivity
        loginRedirectText.setOnClickListener(view -> {
            Log.d(TAG, "Login redirect clicked - going to LoginActivity");
            Intent intent = new Intent(UserTypeSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
            // No finish() to allow back navigation
        });
    }
} 
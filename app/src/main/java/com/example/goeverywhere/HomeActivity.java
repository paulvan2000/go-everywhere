package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    private Button btnBookRide;
    private Button btnPlanTrip;
    private Button btnProfile;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        if (sessionHolder.get() == null) {
            redirectToLogin();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBookRide = findViewById(R.id.btnBookRide);
        btnPlanTrip = findViewById(R.id.btnPlanTrip);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void setupClickListeners() {
        btnBookRide.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnPlanTrip.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PlanActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
} 
package com.example.goeverywhere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SafetyActivity extends AppCompatActivity {

    private CheckBox wheelchairCheckbox;
    private CheckBox animalCheckbox;
    private CheckBox hearingCheckbox;
    private CheckBox visionCheckbox;
    private Button saveButton;
    private ImageButton profileButton;
    private ImageButton homeButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety);

        // Initialize UI elements
        wheelchairCheckbox = findViewById(R.id.wheelchair_checkbox);
        animalCheckbox = findViewById(R.id.animal_checkbox);
        hearingCheckbox = findViewById(R.id.hearing_checkbox);
        visionCheckbox = findViewById(R.id.vision_checkbox);
        saveButton = findViewById(R.id.save_button);
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);

        // Get shared preferences
        preferences = getSharedPreferences("AccessibilityPreferences", MODE_PRIVATE);

        // Load saved preferences
        loadAccessibilityPreferences();

        // Set up button listeners
        saveButton.setOnClickListener(v -> saveAccessibilityPreferences());
        profileButton.setOnClickListener(v -> navigateToProfile());
        homeButton.setOnClickListener(v -> navigateToHome());
    }

    private void loadAccessibilityPreferences() {
        wheelchairCheckbox.setChecked(preferences.getBoolean("wheelchair", false));
        animalCheckbox.setChecked(preferences.getBoolean("service_animal", false));
        hearingCheckbox.setChecked(preferences.getBoolean("hearing", false));
        visionCheckbox.setChecked(preferences.getBoolean("vision", false));
    }

    private void saveAccessibilityPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("wheelchair", wheelchairCheckbox.isChecked());
        editor.putBoolean("service_animal", animalCheckbox.isChecked());
        editor.putBoolean("hearing", hearingCheckbox.isChecked());
        editor.putBoolean("vision", visionCheckbox.isChecked());
        editor.apply();

        Toast.makeText(this, "Accessibility preferences saved", Toast.LENGTH_SHORT).show();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(SafetyActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(SafetyActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
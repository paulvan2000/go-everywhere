package com.example.goeverywhere;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class SafetyAccessibilityActivity extends AppCompatActivity {
    private Switch switchShareLocation;
    private Switch switchTrustedContacts;
    private Switch switchAccessibilityMode;
    private Switch switchVoiceCommands;
    private Button btnEmergencyContacts;
    private Button btnEmergencyServices;
    private Button btnAccessibilitySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_accessibility);

        initializeViews();
        setupClickListeners();
        loadUserPreferences();
    }

    private void initializeViews() {
        switchShareLocation = findViewById(R.id.switchShareLocation);
        switchTrustedContacts = findViewById(R.id.switchTrustedContacts);
        switchAccessibilityMode = findViewById(R.id.switchAccessibilityMode);
        switchVoiceCommands = findViewById(R.id.switchVoiceCommands);
        btnEmergencyContacts = findViewById(R.id.btnEmergencyContacts);
        btnEmergencyServices = findViewById(R.id.btnEmergencyServices);
        btnAccessibilitySettings = findViewById(R.id.btnAccessibilitySettings);
    }

    private void setupClickListeners() {
        switchShareLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateLocationSharingPreference(isChecked);
        });

        switchTrustedContacts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTrustedContactsDialog();
            }
            updateTrustedContactsPreference(isChecked);
        });

        switchAccessibilityMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAccessibilityMode(isChecked);
        });

        switchVoiceCommands.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateVoiceCommandsPreference(isChecked);
        });

        btnEmergencyContacts.setOnClickListener(v -> showEmergencyContactsDialog());
        btnEmergencyServices.setOnClickListener(v -> showEmergencyServicesDialog());
        btnAccessibilitySettings.setOnClickListener(v -> openAccessibilitySettings());
    }

    private void loadUserPreferences() {
        // TODO: Load user preferences from backend/local storage
        // For now, set default values
        switchShareLocation.setChecked(true);
        switchTrustedContacts.setChecked(false);
        switchAccessibilityMode.setChecked(false);
        switchVoiceCommands.setChecked(true);
    }

    private void updateLocationSharingPreference(boolean enabled) {
        // TODO: Update location sharing preference in backend/local storage
    }

    private void updateTrustedContactsPreference(boolean enabled) {
        // TODO: Update trusted contacts preference in backend/local storage
    }

    private void updateAccessibilityMode(boolean enabled) {
        // TODO: Update accessibility mode preference in backend/local storage
        // Apply accessibility settings to the app
    }

    private void updateVoiceCommandsPreference(boolean enabled) {
        // TODO: Update voice commands preference in backend/local storage
    }

    private void showTrustedContactsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Trusted Contacts");
        builder.setMessage("Select contacts who can track your ride progress");
        // TODO: Implement contact selection
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEmergencyContactsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Contacts");
        String[] options = {"Add New Contact", "View/Edit Contacts"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // TODO: Implement add new emergency contact
                    break;
                case 1:
                    // TODO: Implement view/edit emergency contacts
                    break;
            }
        });
        builder.show();
    }

    private void showEmergencyServicesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Emergency Services");
        builder.setMessage("Call emergency services?");
        builder.setPositiveButton("Call 911", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:911"));
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
} 
package com.example.goeverywhere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SafetyActivity extends AppCompatActivity {
    private static final int CALL_PERMISSION_REQUEST_CODE = 1;
    
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
        setContentView(R.layout.activity_safety);

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
            makeEmergencyCall();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    private void makeEmergencyCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
        } else {
            startEmergencyCall();
        }
    }

    private void startEmergencyCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:911"));
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Failed to make emergency call", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startEmergencyCall();
            } else {
                Toast.makeText(this, "Permission denied to make phone calls",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
} 
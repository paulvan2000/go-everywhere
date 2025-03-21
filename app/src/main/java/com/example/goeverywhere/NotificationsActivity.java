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
public class NotificationsActivity extends AppCompatActivity {

    private CheckBox remindersCheckBox;
    private CheckBox notifsCheckBox;
    private Button saveButton;
    private ImageButton profileButton;
    private ImageButton homeButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize views
        remindersCheckBox = findViewById(R.id.reminders_checkbox);
        notifsCheckBox = findViewById(R.id.notifs_checkbox);
        saveButton = findViewById(R.id.saving_button);
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);
        
        // Get shared preferences to save notification settings
        preferences = getSharedPreferences("NotificationPreferences", MODE_PRIVATE);
        
        // Load saved preferences
        remindersCheckBox.setChecked(preferences.getBoolean("reminders", false));
        notifsCheckBox.setChecked(preferences.getBoolean("notifications", false));

        // Set up listeners
        saveButton.setOnClickListener(v -> saveNotificationSettings());
        profileButton.setOnClickListener(v -> navigateToProfile());
        homeButton.setOnClickListener(v -> navigateToHome());
    }

    private void saveNotificationSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("reminders", remindersCheckBox.isChecked());
        editor.putBoolean("notifications", notifsCheckBox.isChecked());
        editor.apply();
        
        Toast.makeText(this, "Notification preferences saved", Toast.LENGTH_SHORT).show();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
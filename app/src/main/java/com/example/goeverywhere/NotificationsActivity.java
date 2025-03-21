package com.example.goeverywhere;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "go_everywhere_channel";
    private static final String CHANNEL_NAME = "Go Everywhere Notifications";
    private static final String CHANNEL_DESC = "Notifications for ride updates and important alerts";

    private Switch switchRideUpdates;
    private Switch switchPromotions;
    private Switch switchPaymentAlerts;
    private Switch switchSafetyAlerts;
    private ListView lvNotifications;
    private List<String> notifications;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        createNotificationChannel();
        initializeViews();
        setupClickListeners();
        loadNotificationPreferences();
        loadNotifications();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initializeViews() {
        switchRideUpdates = findViewById(R.id.switchRideUpdates);
        switchPromotions = findViewById(R.id.switchPromotions);
        switchPaymentAlerts = findViewById(R.id.switchPaymentAlerts);
        switchSafetyAlerts = findViewById(R.id.switchSafetyAlerts);
        lvNotifications = findViewById(R.id.lvNotifications);
        
        notifications = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, 
            notifications);
        lvNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        switchRideUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationPreference("ride_updates", isChecked);
        });

        switchPromotions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationPreference("promotions", isChecked);
        });

        switchPaymentAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationPreference("payment_alerts", isChecked);
        });

        switchSafetyAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationPreference("safety_alerts", isChecked);
        });

        lvNotifications.setOnItemClickListener((parent, view, position, id) -> {
            // TODO: Handle notification click
            // Open relevant activity based on notification type
        });
    }

    private void loadNotificationPreferences() {
        // TODO: Load notification preferences from backend/local storage
        // For now, set default values
        switchRideUpdates.setChecked(true);
        switchPromotions.setChecked(false);
        switchPaymentAlerts.setChecked(true);
        switchSafetyAlerts.setChecked(true);
    }

    private void updateNotificationPreference(String preferenceKey, boolean enabled) {
        // TODO: Update notification preference in backend/local storage
        // This would typically involve an API call to update user preferences
    }

    private void loadNotifications() {
        // TODO: Load notifications from backend
        // For now, add some dummy notifications
        notifications.add("Your driver is 5 minutes away");
        notifications.add("Weekend special: 20% off your next ride!");
        notifications.add("Payment of $25.00 processed successfully");
        notifications.add("Safety tip: Share your ride details with trusted contacts");
        adapter.notifyDataSetChanged();
    }

    public void clearAllNotifications() {
        notifications.clear();
        adapter.notifyDataSetChanged();
        // TODO: Clear notifications in backend
    }

    public void refreshNotifications() {
        // TODO: Fetch new notifications from backend
        loadNotifications();
    }

    // Method to be called from other parts of the app to add new notifications
    public static void addNotification(String message) {
        // TODO: Implement notification handling
        // This would typically involve creating and showing a system notification
        // and updating the notifications list if the activity is active
    }
} 
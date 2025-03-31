package com.example.goeverywhere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentActivity extends AppCompatActivity {

    private Button creditButton;
    private Button debitButton;
    private Button paypalButton;
    private Button applePayButton;
    private ImageButton profileButton;
    private ImageButton homeButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize UI elements
        creditButton = findViewById(R.id.Credit_button);
        debitButton = findViewById(R.id.Debit_button);
        paypalButton = findViewById(R.id.Paypal_button);
        applePayButton = findViewById(R.id.Apple_button);
        profileButton = findViewById(R.id.profile);
        homeButton = findViewById(R.id.home);

        // Get shared preferences
        preferences = getSharedPreferences("PaymentPreferences", MODE_PRIVATE);

        // Set up click listeners
        creditButton.setOnClickListener(v -> selectPaymentMethod("credit_card"));
        debitButton.setOnClickListener(v -> selectPaymentMethod("debit_card"));
        paypalButton.setOnClickListener(v -> selectPaymentMethod("paypal"));
        applePayButton.setOnClickListener(v -> selectPaymentMethod("apple_pay"));
        profileButton.setOnClickListener(v -> navigateToProfile());
        homeButton.setOnClickListener(v -> navigateToHome());

        // Highlight the currently selected payment method
        highlightSelectedPaymentMethod();
    }

    private void selectPaymentMethod(String method) {
        // Save the selected payment method
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("payment_method", method);
        editor.apply();

        Toast.makeText(this, "Payment method updated", Toast.LENGTH_SHORT).show();
        
        // Update UI to highlight the selected method
        highlightSelectedPaymentMethod();
    }

    private void highlightSelectedPaymentMethod() {
        // Get the currently selected payment method
        String currentMethod = preferences.getString("payment_method", "");
        
        // Reset all buttons to default state
        creditButton.setAlpha(0.7f);
        debitButton.setAlpha(0.7f);
        paypalButton.setAlpha(0.7f);
        applePayButton.setAlpha(0.7f);
        
        // Highlight the selected button
        switch (currentMethod) {
            case "credit_card":
                creditButton.setAlpha(1.0f);
                break;
            case "debit_card":
                debitButton.setAlpha(1.0f);
                break;
            case "paypal":
                paypalButton.setAlpha(1.0f);
                break;
            case "apple_pay":
                applePayButton.setAlpha(1.0f);
                break;
        }
    }

    private void navigateToProfile() {
        Intent intent = new Intent(PaymentActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
} 
package com.example.goeverywhere;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class PaymentOptionsActivity extends AppCompatActivity {
    private ListView lvPaymentMethods;
    private Button btnAddPaymentMethod;
    private List<String> paymentMethods;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);

        initializeViews();
        setupPaymentMethodsList();
        setupClickListeners();
    }

    private void initializeViews() {
        lvPaymentMethods = findViewById(R.id.lvPaymentMethods);
        btnAddPaymentMethod = findViewById(R.id.btnAddPaymentMethod);
        paymentMethods = new ArrayList<>();
    }

    private void setupPaymentMethodsList() {
        adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, 
            paymentMethods);
        lvPaymentMethods.setAdapter(adapter);

        // TODO: Load saved payment methods from backend
        // This would typically involve an API call to fetch stored payment methods
    }

    private void setupClickListeners() {
        btnAddPaymentMethod.setOnClickListener(v -> showAddPaymentDialog());

        lvPaymentMethods.setOnItemLongClickListener((parent, view, position, id) -> {
            showPaymentMethodOptions(position);
            return true;
        });
    }

    private void showAddPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Payment Method");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_payment, null);
        EditText etCardNumber = dialogView.findViewById(R.id.etCardNumber);
        EditText etExpiryDate = dialogView.findViewById(R.id.etExpiryDate);
        EditText etCVV = dialogView.findViewById(R.id.etCVV);

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String cardNumber = etCardNumber.getText().toString();
            String expiryDate = etExpiryDate.getText().toString();
            String cvv = etCVV.getText().toString();

            if (validateCardDetails(cardNumber, expiryDate, cvv)) {
                addPaymentMethod(cardNumber, expiryDate);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean validateCardDetails(String cardNumber, String expiryDate, String cvv) {
        // TODO: Implement proper card validation
        return !cardNumber.isEmpty() && !expiryDate.isEmpty() && !cvv.isEmpty();
    }

    private void addPaymentMethod(String cardNumber, String expiryDate) {
        // Mask the card number for display
        String maskedCard = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        paymentMethods.add(maskedCard + " (Expires: " + expiryDate + ")");
        adapter.notifyDataSetChanged();

        // TODO: Send payment method to backend for storage
    }

    private void showPaymentMethodOptions(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Method Options");
        String[] options = {"Set as Default", "Remove"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Set as Default
                    setDefaultPaymentMethod(position);
                    break;
                case 1: // Remove
                    removePaymentMethod(position);
                    break;
            }
        });
        builder.show();
    }

    private void setDefaultPaymentMethod(int position) {
        // TODO: Update backend with new default payment method
        String defaultMethod = paymentMethods.get(position);
        paymentMethods.remove(position);
        paymentMethods.add(0, defaultMethod);
        adapter.notifyDataSetChanged();
    }

    private void removePaymentMethod(int position) {
        // TODO: Remove payment method from backend
        paymentMethods.remove(position);
        adapter.notifyDataSetChanged();
    }
} 
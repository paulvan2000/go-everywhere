package com.example.goeverywhere;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import com.example.goeverywhere.models.PaymentMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {
    private ListView lvPaymentMethods;
    private Button btnAddPaymentMethod;
    private List<PaymentMethod> paymentMethods;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

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
            android.R.layout.simple_list_item_1);
        lvPaymentMethods.setAdapter(adapter);
        loadPaymentMethods();
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
                String cardType = PaymentMethod.detectCardType(cardNumber);
                addPaymentMethod(cardNumber, expiryDate, cardType);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPaymentMethodOptions(int position) {
        PaymentMethod method = paymentMethods.get(position);
        String[] options = {"Set as Default", "Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Method Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    setDefaultPaymentMethod(position);
                    break;
                case 1:
                    removePaymentMethod(position);
                    break;
            }
        });
        builder.show();
    }

    private void setDefaultPaymentMethod(int position) {
        for (PaymentMethod method : paymentMethods) {
            method.setDefault(false);
        }
        paymentMethods.get(position).setDefault(true);
        updatePaymentMethodsDisplay();
        // TODO: Update default payment method in backend
    }

    private void removePaymentMethod(int position) {
        paymentMethods.remove(position);
        updatePaymentMethodsDisplay();
        // TODO: Remove payment method from backend
    }

    private void addPaymentMethod(String cardNumber, String expiryDate, String cardType) {
        PaymentMethod newMethod = new PaymentMethod(
            UUID.randomUUID().toString(),
            cardNumber,
            expiryDate,
            cardType,
            paymentMethods.isEmpty() // First card added is default
        );
        paymentMethods.add(newMethod);
        updatePaymentMethodsDisplay();
        // TODO: Save payment method to backend
    }

    private void updatePaymentMethodsDisplay() {
        adapter.clear();
        for (PaymentMethod method : paymentMethods) {
            String display = method.getCardType() + " " + method.getMaskedCardNumber() +
                    " (Expires: " + method.getExpiryDate() + ")" +
                    (method.isDefault() ? " [Default]" : "");
            adapter.add(display);
        }
    }

    private void loadPaymentMethods() {
        // TODO: Load payment methods from backend
        // For now, add a mock payment method
        if (paymentMethods.isEmpty()) {
            addPaymentMethod("4111111111111111", "12/25", "Visa");
        }
    }

    private boolean validateCardDetails(String cardNumber, String expiryDate, String cvv) {
        // Validate card number (using Luhn algorithm)
        if (!isValidCardNumber(cardNumber)) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate expiry date (MM/YY format)
        if (!isValidExpiryDate(expiryDate)) {
            Toast.makeText(this, "Invalid expiry date (use MM/YY format)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate CVV (3-4 digits)
        if (!isValidCVV(cvv)) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            return false;
        }

        try {
            int month = Integer.parseInt(expiryDate.substring(0, 2));
            int year = Integer.parseInt(expiryDate.substring(3));
            
            if (month < 1 || month > 12) {
                return false;
            }

            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentYear = now.get(java.util.Calendar.YEAR) % 100;
            int currentMonth = now.get(java.util.Calendar.MONTH) + 1;

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3,4}");
    }
} 
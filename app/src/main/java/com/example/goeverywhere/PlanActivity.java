package com.example.goeverywhere;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class PlanActivity extends AppCompatActivity {
    private EditText etPickupLocation;
    private EditText etDropoffLocation;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnPlanTrip;
    private TextView tvSelectedDateTime;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        initializeViews();
        setupClickListeners();
        selectedDateTime = Calendar.getInstance();
    }

    private void initializeViews() {
        etPickupLocation = findViewById(R.id.etPickupLocation);
        etDropoffLocation = findViewById(R.id.etDropoffLocation);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnPlanTrip = findViewById(R.id.btnPlanTrip);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnPlanTrip.setOnClickListener(v -> planTrip());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTimeDisplay();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                updateDateTimeDisplay();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        // Format and display the selected date and time
        String dateTime = String.format("%tF %tR", selectedDateTime, selectedDateTime);
        tvSelectedDateTime.setText(dateTime);
    }

    private void planTrip() {
        String pickup = etPickupLocation.getText().toString();
        String dropoff = etDropoffLocation.getText().toString();
        
        if (!pickup.isEmpty() && !dropoff.isEmpty()) {
            // TODO: Implement backend API call to schedule the future ride
            // This would typically involve sending the ride details to your server
            // including pickup/dropoff locations and scheduled time
        }
    }
} 
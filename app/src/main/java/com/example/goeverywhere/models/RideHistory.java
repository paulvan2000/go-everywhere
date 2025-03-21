package com.example.goeverywhere.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RideHistory {
    private String pickupLocation;
    private String dropoffLocation;
    private Date date;
    private double fare;
    private String status;

    public RideHistory(String pickupLocation, String dropoffLocation, Date date, double fare, String status) {
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.date = date;
        this.fare = fare;
        this.status = status;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public double getFare() {
        return fare;
    }

    public String getStatus() {
        return status;
    }
} 
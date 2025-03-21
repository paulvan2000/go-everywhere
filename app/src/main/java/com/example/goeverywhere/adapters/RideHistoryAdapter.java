package com.example.goeverywhere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.goeverywhere.R;
import com.example.goeverywhere.models.RideHistory;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {
    private List<RideHistory> rideHistories;
    private NumberFormat currencyFormatter;

    public RideHistoryAdapter(List<RideHistory> rideHistories) {
        this.rideHistories = rideHistories;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideHistory ride = rideHistories.get(position);
        holder.tvPickup.setText("From: " + ride.getPickupLocation());
        holder.tvDropoff.setText("To: " + ride.getDropoffLocation());
        holder.tvDate.setText(ride.getFormattedDate());
        holder.tvFare.setText(currencyFormatter.format(ride.getFare()));
        holder.tvStatus.setText(ride.getStatus());
        
        // Set status color based on ride status
        int statusColor;
        switch (ride.getStatus().toLowerCase()) {
            case "completed":
                statusColor = holder.itemView.getContext().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                statusColor = holder.itemView.getContext().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = holder.itemView.getContext().getColor(android.R.color.darker_gray);
        }
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return rideHistories.size();
    }

    public void updateRides(List<RideHistory> newRides) {
        this.rideHistories = newRides;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPickup;
        TextView tvDropoff;
        TextView tvDate;
        TextView tvFare;
        TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvPickup = itemView.findViewById(R.id.tvPickup);
            tvDropoff = itemView.findViewById(R.id.tvDropoff);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
} 
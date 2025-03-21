package com.example.goeverywhere;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RideHistoryActivity extends AppCompatActivity {
    private RecyclerView rvRideHistory;
    private RideHistoryAdapter adapter;
    private List<RideHistoryItem> rideHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        initializeViews();
        setupRecyclerView();
        loadRideHistory();
    }

    private void initializeViews() {
        rvRideHistory = findViewById(R.id.rvRideHistory);
        rideHistory = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new RideHistoryAdapter(rideHistory);
        rvRideHistory.setLayoutManager(new LinearLayoutManager(this));
        rvRideHistory.setAdapter(adapter);
    }

    private void loadRideHistory() {
        // TODO: Load ride history from backend
        // For now, add some dummy data
        rideHistory.add(new RideHistoryItem("123 Main St", "456 Park Ave", "2024-03-13", "$25.00", "Completed"));
        rideHistory.add(new RideHistoryItem("789 Broadway", "321 Fifth Ave", "2024-03-12", "$30.00", "Completed"));
        adapter.notifyDataSetChanged();
    }

    // Inner class for ride history data
    private static class RideHistoryItem {
        String pickup;
        String dropoff;
        String date;
        String fare;
        String status;

        RideHistoryItem(String pickup, String dropoff, String date, String fare, String status) {
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.date = date;
            this.fare = fare;
            this.status = status;
        }
    }

    // Inner class for RecyclerView adapter
    private static class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {
        private List<RideHistoryItem> rideHistory;

        RideHistoryAdapter(List<RideHistoryItem> rideHistory) {
            this.rideHistory = rideHistory;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RideHistoryItem item = rideHistory.get(position);
            holder.tvPickup.setText("From: " + item.pickup);
            holder.tvDropoff.setText("To: " + item.dropoff);
            holder.tvDate.setText("Date: " + item.date);
            holder.tvFare.setText("Fare: " + item.fare);
            holder.tvStatus.setText("Status: " + item.status);
        }

        @Override
        public int getItemCount() {
            return rideHistory.size();
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
} 
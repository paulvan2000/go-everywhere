package com.example.goeverywhere;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.goeverywhere.adapters.RideHistoryAdapter;
import com.example.goeverywhere.models.RideHistory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RideHistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadRideHistory();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.rvRideHistory);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRideHistory);
    }

    private void loadRideHistory() {
        // TODO: Replace with actual API call to fetch ride history
        List<RideHistory> mockData = getMockRideHistory();
        adapter.updateRides(mockData);
        swipeRefreshLayout.setRefreshing(false);
    }

    private List<RideHistory> getMockRideHistory() {
        List<RideHistory> rides = new ArrayList<>();
        rides.add(new RideHistory(
            "123 Main St, City",
            "456 Park Ave, City",
            new Date(),
            25.50,
            "Completed"
        ));
        rides.add(new RideHistory(
            "789 Oak Rd, City",
            "321 Pine St, City",
            new Date(System.currentTimeMillis() - 86400000), // Yesterday
            15.75,
            "Cancelled"
        ));
        // Add more mock data as needed
        return rides;
    }
} 
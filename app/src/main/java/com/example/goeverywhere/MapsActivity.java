package com.example.goeverywhere;

import androidx.fragment.app.FragmentActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference rideRequestsRef;


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            //Redirect to SignupActivity if the user is not authenticated
            Intent intent = new Intent(MapsActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Initialize Firebase reference for ride requests
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("ride_requests");

        //Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Listen for real-time updates from Firebase
        listenForRideRequests();

        //Get latitude and longitude from MainActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);

            if (latitude != 0.0 && longitude != 0.0) {
                //Example origin for testing
                LatLng origin = new LatLng(26.270033371253067, -80.26316008718736);
                LatLng destination = new LatLng(latitude, longitude);

                //Submit the ride request using the destination from MainActivity
                submitRideRequest(origin.latitude + "," + origin.longitude, destination.latitude + "," + destination.longitude);
            } else {
                Toast.makeText(this, "Invalid location data received.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No location data received.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //default origin (FOR TESTING PURPOSES
        LatLng origin = new LatLng(26.270033371253067, -80.26316008718736); // Example origin (my house)

        //destination from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);

            if (latitude != 0.0 && longitude != 0.0) {
                //setting the destination
                LatLng destination = new LatLng(latitude, longitude);

                //Here is where markers are added
                mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
                mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

                //being able to move and zoom the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12));

                //allows the ability to draw the route
                addRoute(origin, destination);

                submitRideRequest(origin.latitude + "," + origin.longitude, destination.latitude + "," + destination.longitude);
            } else {
                Toast.makeText(this, "Invalid destination coordinates received.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No destination data received. Displaying default view.", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12));
        }
    }

    //Submit a ride request to Firebase
    private void submitRideRequest(String origin, String destination) {
        //getting the logged-in user's unique ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        //references user node
        DatabaseReference userRequestsRef = rideRequestsRef.child(userId);

        //checks for user's previous ride requests
        userRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    //removes the last ride request
                    for (DataSnapshot rideRequestSnapshot : snapshot.getChildren()) {
                        rideRequestSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MapsActivity.this, "Previous ride request cleared.", Toast.LENGTH_SHORT).show();
                                    addNewRideRequest(userRequestsRef, origin, destination);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MapsActivity.this, "Failed to clear previous ride request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        break; //ensures that only the first (last added) ride request is removed
                    }
                } else {
                    //No previous ride requests, directly add the new one
                    addNewRideRequest(userRequestsRef, origin, destination);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Failed to check ride request existence: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewRideRequest(DatabaseReference userRequestsRef, String origin, String destination) {
        String requestId = userRequestsRef.push().getKey(); //Generates a unique request id
        RideRequest newRequest = new RideRequest(origin, destination, "pending", System.currentTimeMillis());

        userRequestsRef.child(requestId).setValue(newRequest)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MapsActivity.this, "Ride request submitted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapsActivity.this, "Failed to submit ride request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }





    //Listen for ride requests in real-time
    private void listenForRideRequests() {
        rideRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear(); //Clear existing markers and routes

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RideRequest request = snapshot.getValue(RideRequest.class);

                    if (request != null) {
                        //Display the ride request on the map
                        displayRequestOnMap(request);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Failed to load ride requests.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRequestOnMap(RideRequest request) {
        //Parse origin and destination
        String[] originCoords = request.origin.split(",");
        String[] destinationCoords = request.destination.split(",");
        LatLng origin = new LatLng(Double.parseDouble(originCoords[0]), Double.parseDouble(originCoords[1]));
        LatLng destination = new LatLng(Double.parseDouble(destinationCoords[0]), Double.parseDouble(destinationCoords[1]));

        //Add markers to the map
        mMap.addMarker(new MarkerOptions().position(origin).title("Ride Request Origin"));
        mMap.addMarker(new MarkerOptions().position(destination).title("Ride Request Destination"));

        //Optionally draw a route between the origin and destination
        addRoute(origin, destination);
    }

    private void addRoute(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&key=AIzaSyB0EPNYHnZ86S_VWxbB3LxgAKWA4cCYjHQ";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    JSONArray routes = json.getJSONArray("routes");
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedString = overviewPolyline.getString("points");
                    List<LatLng> decodedPoints = decodePolyline(encodedString);

                    runOnUiThread(() -> mMap.addPolyline(new PolylineOptions().addAll(decodedPoints).width(10).color(Color.BLUE)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            LatLng point = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(point);
        }
        return poly;
    }
}

//RideRequest class
class RideRequest {
    public String origin;
    public String destination;
    public String status;
    public long timestamp;

    public RideRequest() {} // Default constructor required for Firebase

    public RideRequest(String origin, String destination, String status, long timestamp) {
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.timestamp = timestamp;
    }
}

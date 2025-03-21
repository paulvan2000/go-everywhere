package com.example.goeverywhere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.goeverywhere.protocol.grpc.AcceptRideRequest;
import org.example.goeverywhere.protocol.grpc.DriverEvent;
import org.example.goeverywhere.protocol.grpc.DriverServiceGrpc;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.RideRequest;
import org.example.goeverywhere.protocol.grpc.RiderEvent;
import org.example.goeverywhere.protocol.grpc.RiderServiceGrpc;
import org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest;
import org.example.goeverywhere.protocol.grpc.UpdateCurrentLocationRequest;
import org.example.goeverywhere.protocol.grpc.UserServiceGrpc;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AndroidEntryPoint
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Inject
    ManagedChannel managedChannel;

    @Inject
    AtomicReference<LoginResponse> sessionHolder;

    @Override
    protected void onStart() {
        super.onStart();
        if (sessionHolder.get() == null) {
            // Redirect to SignupActivity if the user is not authenticated
            Intent intent = new Intent(MapsActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (sessionHolder.get().getUserType() == UserType.DRIVER) {
            registerForRides();
        } else {
            // Get latitude and longitude from MainActivity
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);
                if (latitude != 0.0 && longitude != 0.0) {
                    // Example origin for testing
                    LatLng origin = new LatLng(26.270033371253067, -80.26316008718736);
                    LatLng destination = new LatLng(latitude, longitude);
                    // Submit the ride request using the destination from MainActivity
                    submitRideRequest(origin.latitude + "," + origin.longitude,
                            destination.latitude + "," + destination.longitude);
                } else {
                    Toast.makeText(this, "Invalid location data received.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No location data received.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Default origin (FOR TESTING PURPOSES)
        LatLng origin = new LatLng(26.270033371253067, -80.26316008718736); // Example origin

        // Destination from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);
            if (latitude != 0.0 && longitude != 0.0) {
                // Setting the destination
                LatLng destination = new LatLng(latitude, longitude);
                // Add markers to the map
                mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
                mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                // Move and zoom the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12));
                // Draw the route
                addRoute(origin, destination);
                // Submit the ride request
                submitRideRequest(origin.latitude + "," + origin.longitude,
                        destination.latitude + "," + destination.longitude);
            } else {
                Toast.makeText(this, "Invalid destination coordinates received.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No destination data received. Displaying default view.", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12));
        }
    }

    // Submit a ride request to the server
    private void submitRideRequest(String origin, String destination) {
        if (sessionHolder.get() == null) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String sessionId = sessionHolder.get().getSessionId();

        RiderServiceGrpc.RiderServiceStub rideService = RiderServiceGrpc.newStub(managedChannel);
        rideService.requestRide(
                RideRequest.newBuilder()
                        .setSessionId(sessionId)
                        .setOrigin(origin)
                        .setDestination(destination)
                        .build(),
                new StreamObserver<RiderEvent>() {
                    @Override
                    public void onNext(RiderEvent rideUpdate) {
                        //for RiderEvent, the proto defines "ride_registered" for a new ride request.
                        switch (rideUpdate.getEventCase()) {
                            case RIDE_REGISTERED:
                                Toast.makeText(MapsActivity.this, "Ride request submitted!", Toast.LENGTH_SHORT).show();
                                break;
                            case DRIVER_ARRIVED:
                                Toast.makeText(MapsActivity.this, "Driver has arrived", Toast.LENGTH_SHORT).show();
                                break;
                            case RIDE_STARTED:
                                Toast.makeText(MapsActivity.this, "Ride started!", Toast.LENGTH_SHORT).show();
                                break;
                            case RIDE_COMPLETED:
                                Toast.makeText(MapsActivity.this, "Ride completed!", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(MapsActivity.this, "Event is not supported yet", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(MapsActivity.this, "Failed to complete the ride request: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted() {
                        Toast.makeText(MapsActivity.this, "The ride is complete.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Notify the server that the driver is available to accept rides and wait for the ride request.
     */
    private void registerForRides() {
        DriverServiceGrpc.DriverServiceStub driverService = DriverServiceGrpc.newStub(managedChannel);
        DriverServiceGrpc.DriverServiceBlockingStub blockingDriverService = DriverServiceGrpc.newBlockingStub(managedChannel);
        UserServiceGrpc.UserServiceBlockingStub userService = UserServiceGrpc.newBlockingStub(managedChannel);

        driverService.subscribeForRideEvents(
                SubscribeForRideEventsRequest.newBuilder()
                        .setSessionId(sessionHolder.get().getSessionId())
                        .build(),
                new StreamObserver<DriverEvent>() {
                    @Override
                    public void onNext(DriverEvent driverEvent) {
                        switch (driverEvent.getEventCase()) {
                            case RIDE_REQUESTED:
                                blockingDriverService.acceptRide(
                                        AcceptRideRequest.newBuilder()
                                                .setSessionId(sessionHolder.get().getSessionId())
                                                .setRideId(driverEvent.getRideRequested().getRideId())
                                                .build());
                                userService.updateCurrentLocation(UpdateCurrentLocationRequest.newBuilder().build());
                                break;
                            case RIDE_CANCELLED:
                                // Rider cancelled the ride, notify the driver as needed
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(MapsActivity.this, "Failed to load ride requests: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted() {
                        Toast.makeText(MapsActivity.this, "Ride requests loaded.", Toast.LENGTH_SHORT).show();
                    }
                });
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
            poly.add(new LatLng(((double) lat / 1E5), ((double) lng / 1E5)));
        }
        return poly;
    }
}

package org.example.goeverywhere.server.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.type.LatLng;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeocodingService {

    private GeoApiContext context;
    @Value("${google.geo.api.key}") String apiKey;


    @PostConstruct
    public void init() {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public LatLng decodeAddress(String address) throws Exception {
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
        if (results.length > 0) {
            com.google.maps.model.LatLng location = results[0].geometry.location;
            return LatLng.newBuilder().setLongitude(location.lng).setLatitude(location.lat).build();
        } else {
            throw new IllegalArgumentException("Unable to decode address: " + address);
        }
    }
}

package org.example.goeverywhere.server.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {

    private final GeoApiContext context;

    @Autowired
    public GeocodingService(@Qualifier("google.geo.api.key") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public LatLng decodeAddress(String address) throws Exception {
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
        if (results.length > 0) {
            return results[0].geometry.location;
        } else {
            throw new IllegalArgumentException("Unable to decode address: " + address);
        }
    }
}

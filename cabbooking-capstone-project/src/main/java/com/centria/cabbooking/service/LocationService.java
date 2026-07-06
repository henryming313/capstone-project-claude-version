package com.centria.cabbooking.service;

import com.centria.cabbooking.dto.response.LocationResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed set of pickup/drop-off points around Kokkola, Finland.
 *
 * The report notes that only location *names* are stored on the backend
 * (trip_bookings.start_location / end_location are VARCHAR), while the
 * lat/lng needed to draw markers on the Leaflet map is a frontend concern.
 * We still expose it from the backend here (rather than hardcoding twice)
 * so the frontend and the fare table always agree on the same location list.
 */
@Service
public class LocationService {

    private final Map<String, double[]> locations = new LinkedHashMap<>();

    public LocationService() {
        locations.put("Centria University of Applied Sciences", new double[]{63.8355, 23.1295});
        locations.put("Kokkola Railway Station", new double[]{63.8412, 23.1339});
        locations.put("Kokkola City Center", new double[]{63.8378, 23.1300});
        locations.put("Kokkola Central Hospital", new double[]{63.8280, 23.1180});
        locations.put("Prisma Kokkola", new double[]{63.8442, 23.1155});
        locations.put("Kokkola-Pietarsaari Airport", new double[]{63.7211, 23.1442});
        locations.put("Kokkola Harbour (Ykspihlaja)", new double[]{63.8580, 23.0630});
        locations.put("Chydenius Library", new double[]{63.8370, 23.1290});
    }

    public List<LocationResponse> listLocations() {
        return locations.entrySet().stream()
                .map(e -> new LocationResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    public boolean isKnownLocation(String name) {
        return locations.containsKey(name);
    }
}

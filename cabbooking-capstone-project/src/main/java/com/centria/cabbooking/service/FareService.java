package com.centria.cabbooking.service;

import com.centria.cabbooking.dto.response.FareEstimateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MVP fare estimation: a fixed base fare plus a fixed surcharge per
 * (pickup, drop-off) route pair - not distance or time based. This
 * mirrors the pricing approach described in the report (e.g. Centria
 * University -> Kokkola Railway Station = 5 EUR base + 8 EUR surcharge
 * = 13 EUR), chosen so the algorithm can later be swapped for a
 * configurable/distance-based table without changing the trip schema.
 */
@Service
public class FareService {

    public static final BigDecimal BASE_FARE = new BigDecimal("5.00");

    private static final String[] ROUTE_ORDER = {
            "Centria University of Applied Sciences",
            "Kokkola Railway Station",
            "Kokkola City Center",
            "Kokkola Central Hospital",
            "Prisma Kokkola",
            "Kokkola-Pietarsaari Airport",
            "Kokkola Harbour (Ykspihlaja)",
            "Chydenius Library"
    };

    // Symmetric surcharge matrix in EUR, indices matching ROUTE_ORDER above.
    private static final int[][] SURCHARGE_MATRIX = {
            {0, 8, 5, 6, 9, 22, 11, 4},
            {8, 0, 4, 7, 10, 20, 13, 5},
            {5, 4, 0, 5, 8, 21, 10, 3},
            {6, 7, 5, 0, 9, 23, 12, 6},
            {9, 10, 8, 9, 0, 19, 14, 8},
            {22, 20, 21, 23, 19, 0, 25, 20},
            {11, 13, 10, 12, 14, 25, 0, 12},
            {4, 5, 3, 6, 8, 20, 12, 0}
    };

    /** Fallback surcharge used only if a location isn't in the known table (kept lightweight, not GPS-based). */
    private static final BigDecimal DEFAULT_SURCHARGE = new BigDecimal("10.00");

    private final Map<String, Integer> indexOf = new LinkedHashMap<>();

    public FareService() {
        for (int i = 0; i < ROUTE_ORDER.length; i++) {
            indexOf.put(ROUTE_ORDER[i], i);
        }
    }

    public FareEstimateResponse estimate(String startLocation, String endLocation) {
        BigDecimal surcharge = lookupSurcharge(startLocation, endLocation);
        BigDecimal total = BASE_FARE.add(surcharge);
        return new FareEstimateResponse(startLocation, endLocation, BASE_FARE, surcharge, total);
    }

    public BigDecimal calculateFare(String startLocation, String endLocation) {
        return BASE_FARE.add(lookupSurcharge(startLocation, endLocation));
    }

    private BigDecimal lookupSurcharge(String startLocation, String endLocation) {
        Integer i = indexOf.get(startLocation);
        Integer j = indexOf.get(endLocation);
        if (i == null || j == null) {
            return DEFAULT_SURCHARGE;
        }
        return new BigDecimal(SURCHARGE_MATRIX[i][j]).setScale(2);
    }
}

package com.centria.cabbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DriverEarningsResponse {
    private Long driverId;
    private long totalCompletedTrips;
    private BigDecimal totalEarnings;
    private BigDecimal averageFare;
    private Double averageRating;
}

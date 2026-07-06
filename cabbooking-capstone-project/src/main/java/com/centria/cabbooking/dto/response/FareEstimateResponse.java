package com.centria.cabbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class FareEstimateResponse {
    private String startLocation;
    private String endLocation;
    private BigDecimal baseFare;
    private BigDecimal surcharge;
    private BigDecimal estimatedFare;
}

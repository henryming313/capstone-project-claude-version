package com.centria.cabbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationResponse {
    private String name;
    private double latitude;
    private double longitude;
}

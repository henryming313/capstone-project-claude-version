package com.centria.cabbooking.dto.response;

import com.centria.cabbooking.common.enums.CabStatus;
import com.centria.cabbooking.entity.Cab;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CabResponse {
    private Long id;
    private String licensePlate;
    private String model;
    private CabStatus status;

    public static CabResponse from(Cab cab) {
        return new CabResponse(cab.getId(), cab.getLicensePlate(), cab.getModel(), cab.getStatus());
    }
}

package com.centria.cabbooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripRejectRequest {

    @NotNull(message = "driverId不能为空")
    private Long driverId;

    private String reason;
}

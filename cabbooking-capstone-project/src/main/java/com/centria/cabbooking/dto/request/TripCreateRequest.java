package com.centria.cabbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripCreateRequest {

    @NotNull(message = "riderId不能为空")
    private Long riderId;

    @NotBlank(message = "起点不能为空")
    private String startLocation;

    @NotBlank(message = "终点不能为空")
    private String endLocation;
}

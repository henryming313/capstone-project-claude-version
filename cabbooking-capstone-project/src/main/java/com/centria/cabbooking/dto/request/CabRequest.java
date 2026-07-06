package com.centria.cabbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CabRequest {

    @NotBlank(message = "车牌号不能为空")
    private String licensePlate;

    @NotBlank(message = "车型不能为空")
    private String model;
}

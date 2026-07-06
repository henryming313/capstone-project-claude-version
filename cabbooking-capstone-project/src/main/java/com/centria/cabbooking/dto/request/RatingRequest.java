package com.centria.cabbooking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {

    @NotNull(message = "tripId不能为空")
    private Long tripId;

    @NotNull(message = "riderId不能为空")
    private Long riderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    @Size(max = 255, message = "评论最长255字符")
    private String comment;
}

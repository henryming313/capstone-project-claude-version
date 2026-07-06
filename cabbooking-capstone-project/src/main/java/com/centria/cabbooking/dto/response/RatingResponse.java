package com.centria.cabbooking.dto.response;

import com.centria.cabbooking.entity.Rating;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private Long tripId;
    private Long driverId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getTrip().getId(),
                rating.getDriver().getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}

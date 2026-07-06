package com.centria.cabbooking.dto.response;

import com.centria.cabbooking.common.enums.TripStatus;
import com.centria.cabbooking.entity.TripBooking;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flattened trip view for the frontend. driverId/driverUsername/cab fields
 * are simply null when a trip has not yet been accepted - the frontend
 * treats absence of a driver as "not yet assigned" rather than an error.
 * (This mirrors a real defect found during functional testing: the
 * original code threw a NullPointerException reading driver fields on a
 * still-PENDING trip.)
 */
@Getter
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private Long riderId;
    private String riderUsername;
    private Long driverId;
    private String driverUsername;
    private Long cabId;
    private String cabLicensePlate;
    private TripStatus status;
    private String startLocation;
    private String endLocation;
    private BigDecimal fare;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TripResponse from(TripBooking trip) {
        return new TripResponse(
                trip.getId(),
                trip.getRider().getId(),
                trip.getRider().getUsername(),
                trip.getDriver() != null ? trip.getDriver().getId() : null,
                trip.getDriver() != null ? trip.getDriver().getUsername() : null,
                trip.getCab() != null ? trip.getCab().getId() : null,
                trip.getCab() != null ? trip.getCab().getLicensePlate() : null,
                trip.getStatus(),
                trip.getStartLocation(),
                trip.getEndLocation(),
                trip.getFare(),
                trip.getCreatedAt(),
                trip.getCompletedAt()
        );
    }
}

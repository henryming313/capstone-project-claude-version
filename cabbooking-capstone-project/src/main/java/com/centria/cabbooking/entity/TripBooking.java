package com.centria.cabbooking.entity;

import com.centria.cabbooking.common.enums.TripStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    /** Null until a driver accepts the trip. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    /** Null until a driver (and their assigned cab) accepts the trip. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cab_id")
    private Cab cab;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.PENDING;

    @Column(name = "start_location", nullable = false, length = 100)
    private String startLocation;

    @Column(name = "end_location", nullable = false, length = 100)
    private String endLocation;

    /** Filled in once the trip is completed. */
    @Column(precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TripStatus.PENDING;
        }
    }
}

package com.centria.cabbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Records that a specific driver rejected a specific trip.
 *
 * A dedicated table (rather than cancelling the trip on rejection) keeps
 * the trip PENDING and visible to other drivers, while making sure the
 * same driver isn't offered a trip they already turned down. See the
 * "handling rejected trips" design debate in the project report.
 */
@Entity
@Table(name = "trip_rejections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripBooking trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(name = "rejected_at", nullable = false, updatable = false)
    private LocalDateTime rejectedAt;

    @Column(length = 255)
    private String reason;

    @PrePersist
    protected void onCreate() {
        this.rejectedAt = LocalDateTime.now();
    }
}

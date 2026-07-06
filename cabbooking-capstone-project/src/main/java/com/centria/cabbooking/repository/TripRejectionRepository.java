package com.centria.cabbooking.repository;

import com.centria.cabbooking.entity.TripRejection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRejectionRepository extends JpaRepository<TripRejection, Long> {

    List<TripRejection> findByTripId(Long tripId);

    boolean existsByTripIdAndDriverId(Long tripId, Long driverId);
}

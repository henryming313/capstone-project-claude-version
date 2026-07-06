package com.centria.cabbooking.repository;

import com.centria.cabbooking.common.enums.TripStatus;
import com.centria.cabbooking.entity.TripBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripBookingRepository extends JpaRepository<TripBooking, Long> {

    List<TripBooking> findByRiderIdOrderByCreatedAtDesc(Long riderId);

    List<TripBooking> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<TripBooking> findByStatusOrderByCreatedAtAsc(TripStatus status);

    long countByDriverIdAndStatus(Long driverId, TripStatus status);

    @Query("select t from TripBooking t where t.status = :status and t.id not in " +
           "(select tr.trip.id from TripRejection tr where tr.driver.id = :driverId)")
    List<TripBooking> findAvailableForDriver(@Param("status") TripStatus status, @Param("driverId") Long driverId);
}

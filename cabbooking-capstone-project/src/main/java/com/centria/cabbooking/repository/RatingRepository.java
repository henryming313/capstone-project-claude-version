package com.centria.cabbooking.repository;

import com.centria.cabbooking.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByTripId(Long tripId);

    boolean existsByTripId(Long tripId);

    List<Rating> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    @Query("select avg(r.score) from Rating r where r.driver.id = :driverId")
    Double findAverageScoreByDriverId(@Param("driverId") Long driverId);
}

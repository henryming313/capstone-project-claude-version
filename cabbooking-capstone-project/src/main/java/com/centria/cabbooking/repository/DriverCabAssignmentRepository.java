package com.centria.cabbooking.repository;

import com.centria.cabbooking.entity.DriverCabAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverCabAssignmentRepository extends JpaRepository<DriverCabAssignment, Long> {

    List<DriverCabAssignment> findByDriverId(Long driverId);

    Optional<DriverCabAssignment> findFirstByDriverIdOrderByAssignedAtDesc(Long driverId);

    boolean existsByDriverIdAndCabId(Long driverId, Long cabId);
}

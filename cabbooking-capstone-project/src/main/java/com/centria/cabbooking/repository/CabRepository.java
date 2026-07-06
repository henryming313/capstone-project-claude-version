package com.centria.cabbooking.repository;

import com.centria.cabbooking.common.enums.CabStatus;
import com.centria.cabbooking.entity.Cab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CabRepository extends JpaRepository<Cab, Long> {

    List<Cab> findByStatus(CabStatus status);

    boolean existsByLicensePlate(String licensePlate);

    Optional<Cab> findByLicensePlate(String licensePlate);
}

package com.centria.cabbooking.service;

import com.centria.cabbooking.common.enums.CabStatus;
import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.dto.request.CabRequest;
import com.centria.cabbooking.dto.response.CabResponse;
import com.centria.cabbooking.entity.Cab;
import com.centria.cabbooking.entity.DriverCabAssignment;
import com.centria.cabbooking.entity.User;
import com.centria.cabbooking.exception.DuplicateResourceException;
import com.centria.cabbooking.exception.ResourceNotFoundException;
import com.centria.cabbooking.repository.CabRepository;
import com.centria.cabbooking.repository.DriverCabAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CabService {

    private final CabRepository cabRepository;
    private final DriverCabAssignmentRepository assignmentRepository;
    private final UserService userService;

    public CabService(CabRepository cabRepository,
                       DriverCabAssignmentRepository assignmentRepository,
                       UserService userService) {
        this.cabRepository = cabRepository;
        this.assignmentRepository = assignmentRepository;
        this.userService = userService;
    }

    @Transactional
    public CabResponse addCab(CabRequest request) {
        if (cabRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new DuplicateResourceException("车牌号已存在: " + request.getLicensePlate());
        }
        Cab cab = new Cab();
        cab.setLicensePlate(request.getLicensePlate());
        cab.setModel(request.getModel());
        cab.setStatus(CabStatus.AVAILABLE);
        return CabResponse.from(cabRepository.save(cab));
    }

    @Transactional(readOnly = true)
    public List<CabResponse> listAll() {
        return cabRepository.findAll().stream().map(CabResponse::from).toList();
    }

    public Cab getCabOrThrow(Long id) {
        return cabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("车辆不存在: id=" + id));
    }

    @Transactional
    public void assignDriverToCab(Long cabId, Long driverId) {
        Cab cab = getCabOrThrow(cabId);
        User driver = userService.requireActiveUserWithRole(driverId, Role.DRIVER);

        if (assignmentRepository.existsByDriverIdAndCabId(driverId, cabId)) {
            throw new DuplicateResourceException("该司机已被分配到此车辆");
        }
        DriverCabAssignment assignment = new DriverCabAssignment();
        assignment.setDriver(driver);
        assignment.setCab(cab);
        assignmentRepository.save(assignment);
    }

    /** The cab most recently assigned to the given driver, if any. */
    public Optional<Cab> findAssignedCab(Long driverId) {
        return assignmentRepository.findFirstByDriverIdOrderByAssignedAtDesc(driverId)
                .map(DriverCabAssignment::getCab);
    }

    @Transactional
    public void updateCabStatus(Long cabId, CabStatus status) {
        Cab cab = getCabOrThrow(cabId);
        cab.setStatus(status);
        cabRepository.save(cab);
    }
}

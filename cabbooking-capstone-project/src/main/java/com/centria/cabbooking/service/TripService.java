package com.centria.cabbooking.service;

import com.centria.cabbooking.common.enums.CabStatus;
import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.common.enums.TripStatus;
import com.centria.cabbooking.dto.request.TripCreateRequest;
import com.centria.cabbooking.dto.response.DriverEarningsResponse;
import com.centria.cabbooking.dto.response.FareEstimateResponse;
import com.centria.cabbooking.dto.response.TripResponse;
import com.centria.cabbooking.entity.Cab;
import com.centria.cabbooking.entity.TripBooking;
import com.centria.cabbooking.entity.TripRejection;
import com.centria.cabbooking.entity.User;
import com.centria.cabbooking.exception.InvalidTripStatusException;
import com.centria.cabbooking.exception.ResourceNotFoundException;
import com.centria.cabbooking.exception.UnauthorizedActionException;
import com.centria.cabbooking.repository.RatingRepository;
import com.centria.cabbooking.repository.TripBookingRepository;
import com.centria.cabbooking.repository.TripRejectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    private final TripBookingRepository tripRepository;
    private final TripRejectionRepository rejectionRepository;
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final CabService cabService;
    private final FareService fareService;
    private final LocationService locationService;

    public TripService(TripBookingRepository tripRepository,
                        TripRejectionRepository rejectionRepository,
                        RatingRepository ratingRepository,
                        UserService userService,
                        CabService cabService,
                        FareService fareService,
                        LocationService locationService) {
        this.tripRepository = tripRepository;
        this.rejectionRepository = rejectionRepository;
        this.ratingRepository = ratingRepository;
        this.userService = userService;
        this.cabService = cabService;
        this.fareService = fareService;
        this.locationService = locationService;
    }

    @Transactional
    public TripResponse createTrip(TripCreateRequest request) {
        User rider = userService.requireActiveUserWithRole(request.getRiderId(), Role.RIDER);

        if (request.getStartLocation().equalsIgnoreCase(request.getEndLocation())) {
            throw new IllegalArgumentException("起点和终点不能相同");
        }
        if (!locationService.isKnownLocation(request.getStartLocation())
                || !locationService.isKnownLocation(request.getEndLocation())) {
            throw new IllegalArgumentException("起点或终点不在支持的站点列表中");
        }

        TripBooking trip = new TripBooking();
        trip.setRider(rider);
        trip.setStartLocation(request.getStartLocation());
        trip.setEndLocation(request.getEndLocation());
        trip.setStatus(TripStatus.PENDING);

        return TripResponse.from(tripRepository.save(trip));
    }

    public FareEstimateResponse estimateFare(String start, String end) {
        return fareService.estimate(start, end);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listForRider(Long riderId) {
        return tripRepository.findByRiderIdOrderByCreatedAtDesc(riderId).stream()
                .map(TripResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listForDriver(Long driverId) {
        return tripRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(TripResponse::from).toList();
    }

    /** Trips still PENDING that the given driver has not already rejected. */
    @Transactional(readOnly = true)
    public List<TripResponse> listAvailableForDriver(Long driverId) {
        return tripRepository.findAvailableForDriver(TripStatus.PENDING, driverId).stream()
                .map(TripResponse::from).toList();
    }

    @Transactional
    public TripResponse acceptTrip(Long tripId, Long driverId) {
        User driver = userService.requireActiveUserWithRole(driverId, Role.DRIVER);
        TripBooking trip = getTripOrThrow(tripId);
        assertTransition(trip, TripStatus.ACCEPTED);

        Cab cab = cabService.findAssignedCab(driverId)
                .orElseThrow(() -> new UnauthorizedActionException("该司机尚未分配车辆，无法接单"));

        trip.setDriver(driver);
        trip.setCab(cab);
        trip.setStatus(TripStatus.ACCEPTED);
        cabService.updateCabStatus(cab.getId(), CabStatus.BUSY);

        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse rejectTrip(Long tripId, Long driverId, String reason) {
        userService.requireActiveUserWithRole(driverId, Role.DRIVER);
        TripBooking trip = getTripOrThrow(tripId);

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new InvalidTripStatusException("只能拒绝处于 PENDING 状态的行程");
        }
        if (rejectionRepository.existsByTripIdAndDriverId(tripId, driverId)) {
            throw new InvalidTripStatusException("你已经拒绝过这个订单");
        }

        TripRejection rejection = new TripRejection();
        rejection.setTrip(trip);
        rejection.setDriver(userService.getUserOrThrow(driverId));
        rejection.setReason(reason);
        rejectionRepository.save(rejection);

        // trip itself stays PENDING so other drivers can still pick it up
        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse startTrip(Long tripId, Long driverId) {
        userService.requireActiveUserWithRole(driverId, Role.DRIVER);
        TripBooking trip = getTripOrThrow(tripId);
        assertOwnedByDriver(trip, driverId);
        assertTransition(trip, TripStatus.IN_PROGRESS);

        trip.setStatus(TripStatus.IN_PROGRESS);
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse completeTrip(Long tripId, Long driverId) {
        userService.requireActiveUserWithRole(driverId, Role.DRIVER);
        TripBooking trip = getTripOrThrow(tripId);
        assertOwnedByDriver(trip, driverId);
        assertTransition(trip, TripStatus.COMPLETED);

        BigDecimal fare = fareService.calculateFare(trip.getStartLocation(), trip.getEndLocation());
        trip.setFare(fare);
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());

        if (trip.getCab() != null) {
            cabService.updateCabStatus(trip.getCab().getId(), CabStatus.AVAILABLE);
        }

        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse cancelTrip(Long tripId, Long riderId) {
        userService.requireActiveUserWithRole(riderId, Role.RIDER);
        TripBooking trip = getTripOrThrow(tripId);

        if (!trip.getRider().getId().equals(riderId)) {
            throw new UnauthorizedActionException("只能取消自己发起的行程");
        }
        assertTransition(trip, TripStatus.CANCELLED);

        if (trip.getCab() != null) {
            cabService.updateCabStatus(trip.getCab().getId(), CabStatus.AVAILABLE);
        }
        trip.setStatus(TripStatus.CANCELLED);
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public DriverEarningsResponse getDriverEarnings(Long driverId) {
        userService.requireActiveUserWithRole(driverId, Role.DRIVER);
        List<TripBooking> completed = tripRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .filter(t -> t.getStatus() == TripStatus.COMPLETED)
                .toList();

        long count = completed.size();
        BigDecimal total = completed.stream()
                .map(TripBooking::getFare)
                .filter(f -> f != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = count == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        Double avgRating = ratingRepository.findAverageScoreByDriverId(driverId);

        return new DriverEarningsResponse(driverId, count, total, average, avgRating);
    }

    private TripBooking getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("行程不存在: id=" + id));
    }

    private void assertTransition(TripBooking trip, TripStatus target) {
        if (!trip.getStatus().canTransitionTo(target)) {
            throw new InvalidTripStatusException(
                    "非法状态转换: " + trip.getStatus() + " -> " + target);
        }
    }

    private void assertOwnedByDriver(TripBooking trip, Long driverId) {
        if (trip.getDriver() == null || !trip.getDriver().getId().equals(driverId)) {
            throw new UnauthorizedActionException("该行程未分配给你，无法操作");
        }
    }
}

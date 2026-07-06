package com.centria.cabbooking.service;

import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.common.enums.TripStatus;
import com.centria.cabbooking.dto.request.RatingRequest;
import com.centria.cabbooking.dto.response.RatingResponse;
import com.centria.cabbooking.entity.Rating;
import com.centria.cabbooking.entity.TripBooking;
import com.centria.cabbooking.exception.InvalidTripStatusException;
import com.centria.cabbooking.exception.ResourceNotFoundException;
import com.centria.cabbooking.exception.UnauthorizedActionException;
import com.centria.cabbooking.repository.RatingRepository;
import com.centria.cabbooking.repository.TripBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TripBookingRepository tripRepository;
    private final UserService userService;

    public RatingService(RatingRepository ratingRepository,
                          TripBookingRepository tripRepository,
                          UserService userService) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
        this.userService = userService;
    }

    @Transactional
    public RatingResponse rateTrip(RatingRequest request) {
        userService.requireActiveUserWithRole(request.getRiderId(), Role.RIDER);

        TripBooking trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("行程不存在: id=" + request.getTripId()));

        if (!trip.getRider().getId().equals(request.getRiderId())) {
            throw new UnauthorizedActionException("只能对自己的行程评分");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new InvalidTripStatusException("只能对已完成的行程评分");
        }
        if (trip.getDriver() == null) {
            throw new InvalidTripStatusException("该行程没有司机信息，无法评分");
        }
        if (ratingRepository.existsByTripId(trip.getId())) {
            throw new InvalidTripStatusException("该行程已经评过分了");
        }

        Rating rating = new Rating();
        rating.setTrip(trip);
        rating.setRider(trip.getRider());
        rating.setDriver(trip.getDriver());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> listForDriver(Long driverId) {
        return ratingRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(RatingResponse::from).toList();
    }
}

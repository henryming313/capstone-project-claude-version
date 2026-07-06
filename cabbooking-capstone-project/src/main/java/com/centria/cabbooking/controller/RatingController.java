package com.centria.cabbooking.controller;

import com.centria.cabbooking.dto.request.RatingRequest;
import com.centria.cabbooking.dto.response.RatingResponse;
import com.centria.cabbooking.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Ratings", description = "乘客对已完成行程评分")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @Operation(summary = "对已完成的行程评分")
    public ResponseEntity<RatingResponse> rate(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.rateTrip(request));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "查看某司机收到的全部评分")
    public List<RatingResponse> listForDriver(@PathVariable Long driverId) {
        return ratingService.listForDriver(driverId);
    }
}

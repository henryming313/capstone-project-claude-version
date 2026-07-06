package com.centria.cabbooking.controller;

import com.centria.cabbooking.dto.request.TripCreateRequest;
import com.centria.cabbooking.dto.request.TripRejectRequest;
import com.centria.cabbooking.dto.response.DriverEarningsResponse;
import com.centria.cabbooking.dto.response.FareEstimateResponse;
import com.centria.cabbooking.dto.response.TripResponse;
import com.centria.cabbooking.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trips", description = "行程全生命周期：预估价格、下单、抢单/拒单、开始/完成、取消、收益统计")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/estimate")
    @Operation(summary = "预估车费")
    public FareEstimateResponse estimate(@RequestParam String startLocation, @RequestParam String endLocation) {
        return tripService.estimateFare(startLocation, endLocation);
    }

    @PostMapping
    @Operation(summary = "乘客发起行程请求")
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.createTrip(request));
    }

    @GetMapping("/rider/{riderId}")
    @Operation(summary = "乘客查看自己的行程历史")
    public List<TripResponse> listForRider(@PathVariable Long riderId) {
        return tripService.listForRider(riderId);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "司机查看自己已接单的行程")
    public List<TripResponse> listForDriver(@PathVariable Long driverId) {
        return tripService.listForDriver(driverId);
    }

    @GetMapping("/available")
    @Operation(summary = "司机查看可接的待处理订单（排除自己已拒绝的）")
    public List<TripResponse> listAvailable(@RequestParam Long driverId) {
        return tripService.listAvailableForDriver(driverId);
    }

    @PutMapping("/{tripId}/accept")
    @Operation(summary = "司机接单")
    public TripResponse accept(@PathVariable Long tripId, @RequestParam Long driverId) {
        return tripService.acceptTrip(tripId, driverId);
    }

    @PutMapping("/{tripId}/reject")
    @Operation(summary = "司机拒单（订单保持PENDING，供其他司机接单）")
    public TripResponse reject(@PathVariable Long tripId, @Valid @RequestBody TripRejectRequest request) {
        return tripService.rejectTrip(tripId, request.getDriverId(), request.getReason());
    }

    @PutMapping("/{tripId}/start")
    @Operation(summary = "司机开始行程")
    public TripResponse start(@PathVariable Long tripId, @RequestParam Long driverId) {
        return tripService.startTrip(tripId, driverId);
    }

    @PutMapping("/{tripId}/complete")
    @Operation(summary = "司机完成行程（计算最终车费）")
    public TripResponse complete(@PathVariable Long tripId, @RequestParam Long driverId) {
        return tripService.completeTrip(tripId, driverId);
    }

    @PutMapping("/{tripId}/cancel")
    @Operation(summary = "乘客取消行程（仅限 PENDING / ACCEPTED 状态）")
    public TripResponse cancel(@PathVariable Long tripId, @RequestParam Long riderId) {
        return tripService.cancelTrip(tripId, riderId);
    }

    @GetMapping("/driver/{driverId}/earnings")
    @Operation(summary = "司机收益统计")
    public DriverEarningsResponse earnings(@PathVariable Long driverId) {
        return tripService.getDriverEarnings(driverId);
    }
}

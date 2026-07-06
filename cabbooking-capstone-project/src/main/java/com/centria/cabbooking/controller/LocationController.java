package com.centria.cabbooking.controller;

import com.centria.cabbooking.dto.response.LocationResponse;
import com.centria.cabbooking.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "预设的 Kokkola 上下车地点及坐标，供地图与下拉框使用")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "获取全部预设地点")
    public List<LocationResponse> list() {
        return locationService.listLocations();
    }
}

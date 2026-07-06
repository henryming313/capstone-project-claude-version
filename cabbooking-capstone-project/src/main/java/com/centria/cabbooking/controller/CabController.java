package com.centria.cabbooking.controller;

import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.dto.request.CabRequest;
import com.centria.cabbooking.dto.response.CabResponse;
import com.centria.cabbooking.service.CabService;
import com.centria.cabbooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cabs")
@Tag(name = "Admin - Cabs", description = "管理员：新增车辆、分配司机")
public class CabController {

    private final CabService cabService;
    private final UserService userService;

    public CabController(CabService cabService, UserService userService) {
        this.cabService = cabService;
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "新增车辆")
    public ResponseEntity<CabResponse> addCab(@Valid @RequestBody CabRequest request, @RequestParam Long adminId) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(cabService.addCab(request));
    }

    @GetMapping
    @Operation(summary = "查看全部车辆")
    public List<CabResponse> listAll(@RequestParam Long adminId) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        return cabService.listAll();
    }

    @PostMapping("/{cabId}/assign/{driverId}")
    @Operation(summary = "将车辆分配给司机")
    public ResponseEntity<Void> assign(@PathVariable Long cabId, @PathVariable Long driverId,
                                        @RequestParam Long adminId) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        cabService.assignDriverToCab(cabId, driverId);
        return ResponseEntity.noContent().build();
    }
}

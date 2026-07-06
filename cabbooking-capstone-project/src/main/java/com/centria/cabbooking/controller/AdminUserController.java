package com.centria.cabbooking.controller;

import com.centria.cabbooking.common.enums.AccountStatus;
import com.centria.cabbooking.common.enums.Role;
import com.centria.cabbooking.dto.response.UserResponse;
import com.centria.cabbooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Users", description = "管理员：查看/封禁/解封用户")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "查看全部用户")
    public List<UserResponse> listAll(@Parameter(description = "管理员用户ID，用于权限校验") @RequestParam Long adminId,
                                       @RequestParam(required = false) Role role) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        return role != null ? userService.listByRole(role) : userService.listAll();
    }

    @PutMapping("/{userId}/ban")
    @Operation(summary = "封禁用户")
    public UserResponse ban(@PathVariable Long userId, @RequestParam Long adminId) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        return userService.setStatus(userId, AccountStatus.BANNED);
    }

    @PutMapping("/{userId}/unban")
    @Operation(summary = "解封用户")
    public UserResponse unban(@PathVariable Long userId, @RequestParam Long adminId) {
        userService.requireActiveUserWithRole(adminId, Role.ADMIN);
        return userService.setStatus(userId, AccountStatus.ACTIVE);
    }
}

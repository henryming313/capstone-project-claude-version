package com.centria.cabbooking.controller;

import com.centria.cabbooking.dto.request.LoginRequest;
import com.centria.cabbooking.dto.request.RegisterRequest;
import com.centria.cabbooking.dto.response.UserResponse;
import com.centria.cabbooking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "注册与登录 (MVP: 无 Session / 无 JWT，前端自行保存返回的用户信息)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "注册新用户 (RIDER / DRIVER / ADMIN)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "登录，返回用户信息供前端本地保存")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

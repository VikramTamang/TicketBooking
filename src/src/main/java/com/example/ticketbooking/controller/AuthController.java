package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.request.LoginRequest;
import com.example.ticketbooking.dto.request.RegisterRequest;
import com.example.ticketbooking.dto.response.ApiResponse;
import com.example.ticketbooking.dto.response.LoginResponse;
import com.example.ticketbooking.dto.response.UserResponse;
import com.example.ticketbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Verifies email + password ONLY. Returns a pendingAuthToken, not a JWT.
     * Phase 6 will add POST /api/v1/auth/verify-otp, which consumes this
     * token and (on success) will be the ONLY path that issues a JWT (Phase 7).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Password verified. OTP verification required.", response));
    }
}
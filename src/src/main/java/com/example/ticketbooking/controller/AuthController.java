package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.request.LoginRequest;
import com.example.ticketbooking.dto.request.RegisterRequest;
import com.example.ticketbooking.dto.request.ResendOtpRequest;
import com.example.ticketbooking.dto.request.VerifyOtpRequest;
import com.example.ticketbooking.dto.response.ApiResponse;
import com.example.ticketbooking.dto.response.LoginResponse;
import com.example.ticketbooking.dto.response.ResendOtpResponse;
import com.example.ticketbooking.dto.response.UserResponse;
import com.example.ticketbooking.service.AuthService;
import com.example.ticketbooking.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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
    private final OtpService otpService;

    @Value("${otp.expiry-minutes:5}")
    private long otpExpiryMinutes;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Password verified. OTP verification required.", response));
    }

    /**
     * Verifies the OTP and marks the PendingAuthSession as otpVerified.
     * Still does NOT issue a JWT - that path only exists in Phase 7,
     * and only accepts sessions where otpVerified is already true.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getPendingAuthToken(), request.getOtpCode());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("OTP verified successfully. You may now request an access token."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<ResendOtpResponse>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        otpService.resendOtp(request.getPendingAuthToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("A new verification code has been sent.",
                        new ResendOtpResponse(otpExpiryMinutes * 60)));
    }
}
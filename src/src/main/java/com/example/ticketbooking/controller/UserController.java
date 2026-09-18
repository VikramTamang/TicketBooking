package com.example.ticketbooking.controller;

import com.example.ticketbooking.dto.response.ApiResponse;
import com.example.ticketbooking.dto.response.UserResponse;
import com.example.ticketbooking.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exists in this phase purely to prove the JWT authentication pipeline
 * works end-to-end: no token -> 401, valid token -> your own data.
 * Real user-facing endpoints (bookings, profile updates, etc.) come later.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal User user) {
        return ApiResponse.success("Current authenticated user", UserResponse.fromEntity(user));
    }
}
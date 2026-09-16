package com.example.ticketbooking.dto.response;

import com.example.ticketbooking.entity.AccountStatus;
import com.example.ticketbooking.entity.Role;
import com.example.ticketbooking.entity.User;

import java.time.Instant;

/**
 * Never includes passwordHash. This is the only representation of a
 * User that ever leaves the backend.
 */
public class UserResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Role role;
    private final AccountStatus accountStatus;
    private final Instant createdAt;

    public UserResponse(Long id, String name, String email, String phoneNumber,
                        Role role, AccountStatus accountStatus, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
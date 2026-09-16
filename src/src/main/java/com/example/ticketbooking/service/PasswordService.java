package com.example.ticketbooking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Single point of contact for password hashing/verification.
 * AuthService (registration) and, from Phase 5 onward, the login flow
 * both go through this class rather than calling PasswordEncoder directly,
 * so there is exactly one place that knows how passwords are hashed/checked.
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Used starting Phase 5 to verify a login attempt against the stored hash.
     * Constant-time comparison is handled internally by BCryptPasswordEncoder.
     */
    public boolean matches(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}
package com.example.ticketbooking.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized security event logging (Section 11 of the spec).
 * Every security-relevant event across the app should go through here,
 * so there is exactly one place to audit for "are we ever logging
 * something we shouldn't be" (passwords, OTPs, tokens).
 *
 * Uses a distinctly named logger ("SECURITY") so log aggregation/routing
 * can filter or forward these events differently from ordinary app logs
 * in a real deployment.
 */
@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY");

    public void loginSuccess(String email) {
        log.info("LOGIN_SUCCESS email={}", email);
    }

    public void loginFailure(String email, String reason) {
        log.warn("LOGIN_FAILURE email={} reason={}", email, reason);
    }

    public void accountLocked(String email, int failedAttempts) {
        log.warn("ACCOUNT_LOCKED email={} failedAttempts={}", email, failedAttempts);
    }
}
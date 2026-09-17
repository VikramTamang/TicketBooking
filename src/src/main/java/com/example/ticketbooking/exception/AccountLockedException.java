package com.example.ticketbooking.exception;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException(Instant lockedUntil) {
        super("Account temporarily locked due to multiple failed login attempts. Try again in "
                + Math.max(1, ChronoUnit.MINUTES.between(Instant.now(), lockedUntil)) + " minute(s).");
    }
}
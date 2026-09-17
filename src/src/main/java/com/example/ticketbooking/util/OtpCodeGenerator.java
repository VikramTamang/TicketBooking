package com.example.ticketbooking.util;

import java.security.SecureRandom;

/**
 * Generates a cryptographically secure, zero-padded 6-digit numeric OTP.
 * Kept separate from SecureTokenGenerator because that one produces
 * Base64 tokens for sessions/refresh tokens, not fixed-length numeric codes.
 */
public final class OtpCodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int UPPER_BOUND = 1_000_000; // 0 - 999999

    private OtpCodeGenerator() {
    }

    public static String generate() {
        int code = SECURE_RANDOM.nextInt(UPPER_BOUND);
        return String.format("%06d", code);
    }
}
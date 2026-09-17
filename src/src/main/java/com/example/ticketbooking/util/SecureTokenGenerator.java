package com.example.ticketbooking.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically secure random tokens for anything
 * security-sensitive: pre-auth session tokens (Phase 5), and later
 * OTP-related secrets (Phase 6) and refresh tokens (Phase 8).
 * Deliberately uses SecureRandom explicitly rather than relying on
 * UUID.randomUUID()'s underlying implementation.
 */
public final class SecureTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureTokenGenerator() {
    }

    /**
     * @param byteLength number of random bytes before Base64 encoding
     *                   (32 bytes = 256 bits of entropy, URL-safe encoded)
     */
    public static String generate(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
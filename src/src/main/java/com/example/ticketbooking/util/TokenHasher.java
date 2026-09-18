package com.example.ticketbooking.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Plain SHA-256 hashing for high-entropy random tokens (refresh tokens).
 *
 * This is deliberately NOT the same mechanism as OtpHasher (HMAC-SHA256).
 * A refresh token is 256 bits of SecureRandom output - brute-forcing a
 * plain hash of it is computationally infeasible regardless of whether an
 * attacker also has a secret key, unlike a 6-digit OTP's tiny search space.
 * Using a keyed HMAC here would add complexity without adding real
 * security margin for this specific value's entropy.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
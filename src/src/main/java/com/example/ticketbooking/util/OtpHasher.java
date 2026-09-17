package com.example.ticketbooking.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * HMAC-SHA256 hashing for OTP codes, keyed by a server-side secret
 * (otp.secret). Using a keyed HMAC instead of a plain SHA-256 hash matters
 * here specifically because the OTP space is tiny (1,000,000 combinations):
 * a plain hash of a leaked otps table could be brute-forced offline in
 * milliseconds. An attacker without the secret key cannot do that, even
 * with the full table in hand.
 */
@Component
public class OtpHasher {

    private final Mac mac;

    public OtpHasher(@Value("${otp.secret}") String secret) {
        try {
            this.mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize OTP hasher", e);
        }
    }

    public synchronized String hash(String code) {
        byte[] result = mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(result);
    }

    /** Constant-time comparison to avoid timing side-channels on OTP verification. */
    public boolean matches(String code, String expectedHash) {
        String actualHash = hash(code);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
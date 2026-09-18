package com.example.ticketbooking.security;

import com.example.ticketbooking.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * The ONLY component in the application that creates or cryptographically
 * validates access tokens. Claims are kept minimal per Section 12:
 * subject = user id, plus a "role" claim needed for authorization checks
 * without a DB round trip for the role itself (though the filter still
 * hits the DB for account-status revocation, see Phase 7 notes).
 */
@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenTtlMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes) {
        // HMAC-SHA256 requires a key of at least 256 bits (32 bytes).
        // A short/weak secret here would make tokens forgeable.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes long for HS256. Current length: " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenTtlMinutes * 60);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlMinutes * 60;
    }

    /**
     * Returns parsed claims if the token is structurally valid, correctly
     * signed, and not expired. Returns empty otherwise - callers never
     * see raw JWT exceptions, they just treat it as "not authenticated".
     */
    public Optional<Claims> parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException | JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
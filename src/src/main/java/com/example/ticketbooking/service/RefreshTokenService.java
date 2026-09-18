package com.example.ticketbooking.service;

import com.example.ticketbooking.entity.RefreshToken;
import com.example.ticketbooking.entity.User;
import com.example.ticketbooking.exception.RefreshTokenExpiredException;
import com.example.ticketbooking.exception.RefreshTokenInvalidException;
import com.example.ticketbooking.exception.RefreshTokenReusedException;
import com.example.ticketbooking.repository.RefreshTokenRepository;
import com.example.ticketbooking.security.SecurityEventLogger;
import com.example.ticketbooking.util.SecureTokenGenerator;
import com.example.ticketbooking.util.TokenHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityEventLogger securityEventLogger;

    @Value("${jwt.refresh-token-ttl-days:30}")
    private long refreshTokenTtlDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               SecurityEventLogger securityEventLogger) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityEventLogger = securityEventLogger;
    }

    /**
     * Issues the FIRST refresh token of a brand-new family. Called only
     * from AuthService.issueAccessToken, i.e. only right after a JWT
     * access token is created from a fully verified (OTP-passed) session.
     */
    @Transactional
    public String issueNewFamily(User user) {
        String familyId = UUID.randomUUID().toString();
        return createToken(user, familyId);
    }

    /**
     * Rotates a refresh token: validates the submitted raw token, and if
     * valid, revokes it and issues a new one in the same family.
     *
     * Reuse detection: if the submitted token's hash matches a RefreshToken
     * row that is ALREADY revoked, that means rotation already happened
     * once for this token - so this submission is either a replay attack
     * or a stolen/duplicated token. The entire family is revoked in
     * response, forcing the legitimate user to log in again.
     */
    @Transactional
    public RotationResult rotate(String rawRefreshToken) {
        String hash = TokenHasher.hash(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(RefreshTokenInvalidException::new);

        if (existing.isRevoked()) {
            // Reuse of an already-rotated-away token: treat as compromise.
            refreshTokenRepository.revokeAllInFamily(existing.getFamilyId(), Instant.now());
            securityEventLogger.refreshTokenReuseDetected(existing.getUser().getEmail(), existing.getFamilyId());
            throw new RefreshTokenReusedException();
        }

        if (existing.isExpired()) {
            throw new RefreshTokenExpiredException();
        }

        existing.setRevoked(true);
        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);

        String newRawToken = createToken(existing.getUser(), existing.getFamilyId());
        securityEventLogger.refreshTokenRotated(existing.getUser().getEmail());

        return new RotationResult(existing.getUser(), newRawToken);
    }

    /** Logout: revoke the entire family so no earlier token in the chain remains usable. */
    @Transactional
    public void revokeFamilyByRawToken(String rawRefreshToken) {
        String hash = TokenHasher.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            refreshTokenRepository.revokeAllInFamily(token.getFamilyId(), Instant.now());
            securityEventLogger.refreshTokenRevoked(token.getUser().getEmail(), token.getFamilyId());
        });
        // Deliberately silent if not found: logout should not leak whether
        // a given refresh token ever existed.
    }

    private String createToken(User user, String familyId) {
        String rawToken = SecureTokenGenerator.generate(32);
        String hash = TokenHasher.hash(rawToken);
        Instant expiresAt = Instant.now().plus(refreshTokenTtlDays, ChronoUnit.DAYS);

        RefreshToken token = new RefreshToken(user, hash, familyId, expiresAt);
        refreshTokenRepository.save(token);

        return rawToken;
    }

    public record RotationResult(User user, String newRawRefreshToken) {
    }
}
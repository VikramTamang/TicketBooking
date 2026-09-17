package com.example.ticketbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Represents the "temporary authentication state" required by Section 7:
 * created after password verification succeeds, but BEFORE 2FA/JWT.
 * This token grants NO API access on its own — Phase 6/7 will only accept
 * it as input to the OTP-verification endpoint, never as a Bearer token.
 *
 * Cleanup of expired rows is handled by a scheduled job in Phase 24;
 * until then, expiry is enforced logically (checked at read time) even
 * if the row still physically exists in the table.
 */
@Entity
@Table(name = "pending_auth_sessions")
public class PendingAuthSession {

    @Id
    @Column(length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Set true once consumed by OTP verification (Phase 6), so a
     * pre-auth token can never be reused even if not yet expired.
     */
    @Column(nullable = false)
    private boolean used = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected PendingAuthSession() {
    }

    public PendingAuthSession(String token, User user, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
package com.example.ticketbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * One active OTP per PendingAuthSession at a time. Resending deletes the
 * previous row and creates a new one, so "previous OTP invalidated when a
 * new OTP is generated" (Section 9) is enforced simply by there only ever
 * being one row per session.
 *
 * The OTP code itself is never stored in plaintext — only an HMAC-SHA256
 * hash. Unlike a plain hash, HMAC requires the server-side secret key to
 * verify, so even a full row (hash) leak does not let an attacker brute
 * force the 1,000,000 possible 6-digit codes offline.
 */
@Entity
@Table(name = "otps")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_token", referencedColumnName = "token", nullable = false, unique = true)
    private PendingAuthSession pendingAuthSession;

    @Column(nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant resendAvailableAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Otp() {
    }

    public Otp(PendingAuthSession pendingAuthSession, String codeHash, Instant expiresAt, Instant resendAvailableAt) {
        this.pendingAuthSession = pendingAuthSession;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.resendAvailableAt = resendAvailableAt;
    }

    public Long getId() {
        return id;
    }

    public PendingAuthSession getPendingAuthSession() {
        return pendingAuthSession;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getResendAvailableAt() {
        return resendAvailableAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
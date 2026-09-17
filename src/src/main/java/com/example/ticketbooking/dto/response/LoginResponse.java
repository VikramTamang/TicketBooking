package com.example.ticketbooking.dto.response;

/**
 * Deliberately contains NO access token / JWT field. This is a structural
 * guarantee (Section 7's CRITICAL SECURITY RULE) that password verification
 * alone cannot yield API access — the type itself doesn't have the field.
 */
public class LoginResponse {

    private final String pendingAuthToken;
    private final boolean otpRequired;
    private final long expiresInSeconds;

    public LoginResponse(String pendingAuthToken, boolean otpRequired, long expiresInSeconds) {
        this.pendingAuthToken = pendingAuthToken;
        this.otpRequired = otpRequired;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getPendingAuthToken() {
        return pendingAuthToken;
    }

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
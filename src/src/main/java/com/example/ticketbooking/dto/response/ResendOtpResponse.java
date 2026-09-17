package com.example.ticketbooking.dto.response;

public class ResendOtpResponse {

    private final long expiresInSeconds;

    public ResendOtpResponse(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
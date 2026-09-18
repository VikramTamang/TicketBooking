package com.example.ticketbooking.dto.response;

public class AuthTokenResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresInSeconds;

    public AuthTokenResponse(String accessToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
package com.example.ticketbooking.exception;

public class RefreshTokenReusedException extends RuntimeException {

    public RefreshTokenReusedException() {
        super("Suspicious activity detected on this session. Please log in again.");
    }
}
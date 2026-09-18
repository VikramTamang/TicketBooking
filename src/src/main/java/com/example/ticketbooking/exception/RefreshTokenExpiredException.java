package com.example.ticketbooking.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException() {
        super("Refresh token has expired. Please log in again.");
    }
}
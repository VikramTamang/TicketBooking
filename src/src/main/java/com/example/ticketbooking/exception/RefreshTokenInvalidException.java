package com.example.ticketbooking.exception;

public class RefreshTokenInvalidException extends RuntimeException {

    public RefreshTokenInvalidException() {
        super("Refresh token is invalid. Please log in again.");
    }
}
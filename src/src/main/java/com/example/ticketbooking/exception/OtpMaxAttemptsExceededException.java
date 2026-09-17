package com.example.ticketbooking.exception;

public class OtpMaxAttemptsExceededException extends RuntimeException {

    public OtpMaxAttemptsExceededException() {
        super("Too many incorrect attempts. Please request a new verification code.");
    }
}
package com.example.ticketbooking.exception;

public class OtpExpiredException extends RuntimeException {

    public OtpExpiredException() {
        super("Your verification code has expired. Please request a new one.");
    }
}
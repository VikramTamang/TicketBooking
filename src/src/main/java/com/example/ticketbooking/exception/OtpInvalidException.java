package com.example.ticketbooking.exception;

public class OtpInvalidException extends RuntimeException {

    public OtpInvalidException(String message) {
        super(message);
    }
}
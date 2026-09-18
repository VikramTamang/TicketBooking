package com.example.ticketbooking.exception;

public class SessionNotVerifiedException extends RuntimeException {

    public SessionNotVerifiedException() {
        super("This session has not completed OTP verification.");
    }
}
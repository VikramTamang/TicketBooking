package com.example.ticketbooking.exception;

public class OtpResendCooldownException extends RuntimeException {

    public OtpResendCooldownException(long secondsRemaining) {
        super("Please wait " + secondsRemaining + " second(s) before requesting another code.");
    }
}
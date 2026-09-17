package com.example.ticketbooking.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("This account has been disabled. Please contact support.");
    }
}
package com.example.ticketbooking.exception;

public class PendingAuthSessionInvalidException extends RuntimeException {

    public PendingAuthSessionInvalidException() {
        super("Your login session has expired or is invalid. Please log in again.");
    }
}
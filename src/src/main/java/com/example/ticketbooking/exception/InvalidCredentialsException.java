package com.example.ticketbooking.exception;

/**
 * Thrown for both "email not found" and "wrong password" cases.
 * The message is intentionally identical for both scenarios (OWASP:
 * do not allow user enumeration via differing error responses).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
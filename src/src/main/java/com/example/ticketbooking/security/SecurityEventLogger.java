package com.example.ticketbooking.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY");

    public void loginSuccess(String email) {
        log.info("LOGIN_SUCCESS email={}", email);
    }

    public void loginFailure(String email, String reason) {
        log.warn("LOGIN_FAILURE email={} reason={}", email, reason);
    }

    public void accountLocked(String email, int failedAttempts) {
        log.warn("ACCOUNT_LOCKED email={} failedAttempts={}", email, failedAttempts);
    }

    public void otpSent(String email) {
        log.info("OTP_SENT email={}", email);
    }

    public void otpResent(String email) {
        log.info("OTP_RESENT email={}", email);
    }

    public void otpVerifySuccess(String email) {
        log.info("OTP_VERIFY_SUCCESS email={}", email);
    }

    public void otpVerifyFailure(String email, String reason) {
        log.warn("OTP_VERIFY_FAILURE email={} reason={}", email, reason);
    }

    public void otpMaxAttemptsExceeded(String email) {
        log.warn("OTP_MAX_ATTEMPTS_EXCEEDED email={}", email);
    }
}
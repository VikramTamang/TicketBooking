package com.example.ticketbooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Validation failed");
        body.put("errorCode", "VALIDATION_ERROR");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage(), "EMAIL_ALREADY_EXISTS");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<Map<String, Object>> handleAccountDisabled(AccountDisabledException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCOUNT_DISABLED");
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(AccountLockedException ex) {
        return errorResponse(HttpStatus.LOCKED, ex.getMessage(), "ACCOUNT_LOCKED");
    }

    @ExceptionHandler(PendingAuthSessionInvalidException.class)
    public ResponseEntity<Map<String, Object>> handlePendingSessionInvalid(PendingAuthSessionInvalidException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "SESSION_INVALID");
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleOtpInvalid(OtpInvalidException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "OTP_INVALID");
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleOtpExpired(OtpExpiredException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "OTP_EXPIRED");
    }

    @ExceptionHandler(OtpMaxAttemptsExceededException.class)
    public ResponseEntity<Map<String, Object>> handleOtpMaxAttempts(OtpMaxAttemptsExceededException ex) {
        return errorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), "OTP_MAX_ATTEMPTS_EXCEEDED");
    }

    @ExceptionHandler(OtpResendCooldownException.class)
    public ResponseEntity<Map<String, Object>> handleOtpResendCooldown(OtpResendCooldownException ex) {
        return errorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), "OTP_RESEND_COOLDOWN");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_ERROR");
    }

    @ExceptionHandler(SessionNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleSessionNotVerified(SessionNotVerifiedException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "OTP_NOT_VERIFIED");
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message, String errorCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("errorCode", errorCode);
        return ResponseEntity.status(status).body(body);
    }
}
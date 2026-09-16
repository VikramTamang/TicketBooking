package com.example.ticketbooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enforces:
 *  - length between 8 and 72 bytes (BCrypt silently truncates beyond 72 bytes
 *    of UTF-8 input; rejecting up front avoids a confusing situation where a
 *    user's very long password is "accepted" but only part of it actually
 *    matters for authentication)
 *  - at least one uppercase, one lowercase, one digit, one special character
 *  - not present in a common/known-weak password list (case-insensitive)
 *
 * Loaded once at class-init time; the blocklist file is small enough to
 * hold entirely in memory rather than hitting the DB or an external API
 * on every registration/password-change request.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[@$!%*?&#^()_+=\\-]");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72; // BCrypt's hard input limit

    private static final Set<String> COMMON_PASSWORDS = loadCommonPasswords();

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        if (password.length() < MIN_LENGTH) {
            addViolation(context, "Password must be at least " + MIN_LENGTH + " characters long");
            return false;
        }

        int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > MAX_BYTES) {
            addViolation(context, "Password is too long (max " + MAX_BYTES + " bytes)");
            return false;
        }

        if (!UPPERCASE.matcher(password).find()
                || !LOWERCASE.matcher(password).find()
                || !DIGIT.matcher(password).find()
                || !SPECIAL.matcher(password).find()) {
            addViolation(context, "Password must contain uppercase, lowercase, a digit, and a special character");
            return false;
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            addViolation(context, "This password is too common; please choose a different one");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private static Set<String> loadCommonPasswords() {
        Set<String> passwords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("common-passwords.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    passwords.add(trimmed.toLowerCase());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load common-passwords.txt", e);
        }
        return passwords;
    }
}
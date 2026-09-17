package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.request.LoginRequest;
import com.example.ticketbooking.dto.request.RegisterRequest;
import com.example.ticketbooking.dto.response.LoginResponse;
import com.example.ticketbooking.dto.response.UserResponse;
import com.example.ticketbooking.entity.AccountStatus;
import com.example.ticketbooking.entity.PendingAuthSession;
import com.example.ticketbooking.entity.Role;
import com.example.ticketbooking.entity.User;
import com.example.ticketbooking.exception.AccountDisabledException;
import com.example.ticketbooking.exception.AccountLockedException;
import com.example.ticketbooking.exception.DuplicateEmailException;
import com.example.ticketbooking.exception.InvalidCredentialsException;
import com.example.ticketbooking.repository.PendingAuthSessionRepository;
import com.example.ticketbooking.repository.UserRepository;
import com.example.ticketbooking.security.SecurityEventLogger;
import com.example.ticketbooking.util.SecureTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PendingAuthSessionRepository pendingAuthSessionRepository;
    private final PasswordService passwordService;
    private final SecurityEventLogger securityEventLogger;

    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lockout-duration-minutes:15}")
    private long lockoutDurationMinutes;

    @Value("${security.login.pending-auth-ttl-minutes:5}")
    private long pendingAuthTtlMinutes;

    /**
     * A fixed, valid-looking BCrypt hash of a value nobody will ever guess.
     * Used only to run a "dummy" password comparison when the email doesn't
     * exist, so that the response time for "unknown email" and "wrong
     * password for a real account" is similar — reduces (does not fully
     * eliminate) the ability to enumerate valid emails via timing.
     */
    private static final String DUMMY_HASH =
            "$2a$10$C6UzMDM.H6dfI/f/IKcEeO6Zu5ZqQ7vX0G9V0aFhtY.tzWFoR6H3q";

    public AuthService(UserRepository userRepository,
                       PendingAuthSessionRepository pendingAuthSessionRepository,
                       PasswordService passwordService,
                       SecurityEventLogger securityEventLogger) {
        this.userRepository = userRepository;
        this.pendingAuthSessionRepository = pendingAuthSessionRepository;
        this.passwordService = passwordService;
        this.securityEventLogger = securityEventLogger;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String hashedPassword = passwordService.hash(request.getPassword());

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                request.getPhoneNumber().trim(),
                Role.USER,
                AccountStatus.ACTIVE
        );

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty()) {
            // Run a dummy hash comparison so response timing doesn't
            // obviously differ from the "wrong password" path below.
            passwordService.matches(request.getPassword(), DUMMY_HASH);
            securityEventLogger.loginFailure(normalizedEmail, "EMAIL_NOT_FOUND");
            throw new InvalidCredentialsException();
        }

        User user = userOpt.get();

        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            securityEventLogger.loginFailure(normalizedEmail, "ACCOUNT_DISABLED");
            throw new AccountDisabledException();
        }

        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            securityEventLogger.loginFailure(normalizedEmail, "ACCOUNT_LOCKED");
            throw new AccountLockedException(user.getLockedUntil());
        }

        boolean passwordMatches = passwordService.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            handleFailedAttempt(user);
            securityEventLogger.loginFailure(normalizedEmail, "WRONG_PASSWORD");
            throw new InvalidCredentialsException();
        }

        // Successful password verification: reset lockout counters,
        // but DO NOT issue a JWT here. Only create the temporary
        // pre-auth state, per Section 7's non-negotiable rule.
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        String token = SecureTokenGenerator.generate(32);
        Instant expiresAt = Instant.now().plus(pendingAuthTtlMinutes, ChronoUnit.MINUTES);
        PendingAuthSession session = new PendingAuthSession(token, user, expiresAt);
        pendingAuthSessionRepository.save(session);

        securityEventLogger.loginSuccess(normalizedEmail);

        return new LoginResponse(token, true, pendingAuthTtlMinutes * 60);
    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            Instant lockUntil = Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
            user.setLockedUntil(lockUntil);
            securityEventLogger.accountLocked(user.getEmail(), attempts);
        }

        userRepository.save(user);
    }
}
package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.request.LoginRequest;
import com.example.ticketbooking.dto.request.RegisterRequest;
import com.example.ticketbooking.dto.response.AuthTokenResponse;
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
import com.example.ticketbooking.exception.PendingAuthSessionInvalidException;
import com.example.ticketbooking.exception.SessionNotVerifiedException;
import com.example.ticketbooking.repository.PendingAuthSessionRepository;
import com.example.ticketbooking.repository.UserRepository;
import com.example.ticketbooking.security.JwtService;
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
    private final OtpService otpService;
    private final JwtService jwtService;

    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lockout-duration-minutes:15}")
    private long lockoutDurationMinutes;

    @Value("${security.login.pending-auth-ttl-minutes:5}")
    private long pendingAuthTtlMinutes;

    private static final String DUMMY_HASH =
            "$2a$10$C6UzMDM.H6dfI/f/IKcEeO6Zu5ZqQ7vX0G9V0aFhtY.tzWFoR6H3q";

    public AuthService(UserRepository userRepository,
                       PendingAuthSessionRepository pendingAuthSessionRepository,
                       PasswordService passwordService,
                       SecurityEventLogger securityEventLogger,
                       OtpService otpService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.pendingAuthSessionRepository = pendingAuthSessionRepository;
        this.passwordService = passwordService;
        this.securityEventLogger = securityEventLogger;
        this.otpService = otpService;
        this.jwtService = jwtService;
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

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        String token = SecureTokenGenerator.generate(32);
        Instant expiresAt = Instant.now().plus(pendingAuthTtlMinutes, ChronoUnit.MINUTES);
        PendingAuthSession session = new PendingAuthSession(token, user, expiresAt);
        pendingAuthSessionRepository.save(session);

        otpService.generateAndSendOtp(session);

        securityEventLogger.loginSuccess(normalizedEmail);

        return new LoginResponse(token, true, pendingAuthTtlMinutes * 60);
    }

    /**
     * The ONLY method in the entire codebase that creates a JWT.
     * Requires a PendingAuthSession that is: found, not expired, not
     * already used, AND already otpVerified=true. Any other state throws.
     * On success, marks the session used=true so it can never be
     * exchanged for a second token.
     */
    @Transactional
    public AuthTokenResponse issueAccessToken(String pendingAuthToken) {
        PendingAuthSession session = pendingAuthSessionRepository
                .findByTokenAndUsedFalse(pendingAuthToken)
                .orElseThrow(PendingAuthSessionInvalidException::new);

        if (session.isExpired()) {
            throw new PendingAuthSessionInvalidException();
        }

        if (!session.isOtpVerified()) {
            securityEventLogger.loginFailure(session.getUser().getEmail(), "TOKEN_REQUESTED_WITHOUT_OTP");
            throw new SessionNotVerifiedException();
        }

        session.setUsed(true);
        pendingAuthSessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(session.getUser());
        securityEventLogger.loginSuccess(session.getUser().getEmail());

        return new AuthTokenResponse(accessToken, jwtService.getAccessTokenTtlSeconds());
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
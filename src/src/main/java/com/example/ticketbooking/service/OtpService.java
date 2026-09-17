package com.example.ticketbooking.service;

import com.example.ticketbooking.entity.Otp;
import com.example.ticketbooking.entity.PendingAuthSession;
import com.example.ticketbooking.exception.OtpExpiredException;
import com.example.ticketbooking.exception.OtpInvalidException;
import com.example.ticketbooking.exception.OtpMaxAttemptsExceededException;
import com.example.ticketbooking.exception.OtpResendCooldownException;
import com.example.ticketbooking.exception.PendingAuthSessionInvalidException;
import com.example.ticketbooking.repository.OtpRepository;
import com.example.ticketbooking.repository.PendingAuthSessionRepository;
import com.example.ticketbooking.security.SecurityEventLogger;
import com.example.ticketbooking.service.email.EmailService;
import com.example.ticketbooking.util.OtpCodeGenerator;
import com.example.ticketbooking.util.OtpHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final PendingAuthSessionRepository pendingAuthSessionRepository;
    private final OtpHasher otpHasher;
    private final EmailService emailService;
    private final SecurityEventLogger securityEventLogger;

    @Value("${otp.expiry-minutes:5}")
    private long expiryMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public OtpService(OtpRepository otpRepository,
                      PendingAuthSessionRepository pendingAuthSessionRepository,
                      OtpHasher otpHasher,
                      EmailService emailService,
                      SecurityEventLogger securityEventLogger) {
        this.otpRepository = otpRepository;
        this.pendingAuthSessionRepository = pendingAuthSessionRepository;
        this.otpHasher = otpHasher;
        this.emailService = emailService;
        this.securityEventLogger = securityEventLogger;
    }

    /** Called by AuthService right after a PendingAuthSession is created at login. */
    @Transactional
    public void generateAndSendOtp(PendingAuthSession session) {
        // Enforces "previous OTP invalidated when a new one is generated":
        // there is at most one Otp row per session at any time.
        otpRepository.findByPendingAuthSession_Token(session.getToken())
                .ifPresent(otpRepository::delete);

        String code = OtpCodeGenerator.generate();
        String codeHash = otpHasher.hash(code);
        Instant now = Instant.now();

        Otp otp = new Otp(
                session,
                codeHash,
                now.plus(expiryMinutes, ChronoUnit.MINUTES),
                now.plusSeconds(resendCooldownSeconds)
        );
        otpRepository.save(otp);

        emailService.sendOtpEmail(session.getUser().getEmail(), code, expiryMinutes);
        securityEventLogger.otpSent(session.getUser().getEmail());
    }

    @Transactional
    public void resendOtp(String pendingAuthToken) {
        PendingAuthSession session = loadActiveSession(pendingAuthToken);

        otpRepository.findByPendingAuthSession_Token(session.getToken()).ifPresent(existing -> {
            if (Instant.now().isBefore(existing.getResendAvailableAt())) {
                long secondsRemaining = ChronoUnit.SECONDS.between(Instant.now(), existing.getResendAvailableAt());
                throw new OtpResendCooldownException(Math.max(1, secondsRemaining));
            }
        });

        generateAndSendOtp(session);
        securityEventLogger.otpResent(session.getUser().getEmail());
    }

    @Transactional
    public void verifyOtp(String pendingAuthToken, String submittedCode) {
        PendingAuthSession session = loadActiveSession(pendingAuthToken);
        String email = session.getUser().getEmail();

        Otp otp = otpRepository.findByPendingAuthSession_Token(session.getToken())
                .orElseThrow(() -> {
                    securityEventLogger.otpVerifyFailure(email, "NO_OTP_FOUND");
                    return new OtpInvalidException("No verification code found. Please request a new one.");
                });

        if (otp.isUsed()) {
            securityEventLogger.otpVerifyFailure(email, "ALREADY_USED");
            throw new OtpInvalidException("This verification code has already been used.");
        }

        if (otp.getAttempts() >= maxAttempts) {
            securityEventLogger.otpMaxAttemptsExceeded(email);
            throw new OtpMaxAttemptsExceededException();
        }

        if (otp.isExpired()) {
            securityEventLogger.otpVerifyFailure(email, "EXPIRED");
            throw new OtpExpiredException();
        }

        if (!otpHasher.matches(submittedCode, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            securityEventLogger.otpVerifyFailure(email, "WRONG_CODE");
            throw new OtpInvalidException("Incorrect verification code.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        session.setOtpVerified(true);
        pendingAuthSessionRepository.save(session);

        securityEventLogger.otpVerifySuccess(email);
    }

    private PendingAuthSession loadActiveSession(String pendingAuthToken) {
        PendingAuthSession session = pendingAuthSessionRepository.findByTokenAndUsedFalse(pendingAuthToken)
                .orElseThrow(PendingAuthSessionInvalidException::new);

        if (session.isExpired()) {
            throw new PendingAuthSessionInvalidException();
        }

        return session;
    }
}
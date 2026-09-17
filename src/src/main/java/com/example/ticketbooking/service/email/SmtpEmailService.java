package com.example.ticketbooking.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Real email delivery via SMTP, active only on the "prod" profile.
 * Credentials come from environment variables (MAIL_USERNAME / MAIL_PASSWORD,
 * per Section 36) bound through application-prod.yml in Phase 37 - never
 * hardcoded here.
 */
@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, long expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your verification code");
        message.setText("Your one-time verification code is: " + otpCode
                + "\nThis code expires in " + expiryMinutes + " minute(s).\n"
                + "If you did not request this, you can safely ignore this email.");

        try {
            mailSender.send(message);
            log.info("OTP email dispatched successfully"); // never log the recipient+code together in prod logs
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            throw new IllegalStateException("Failed to send verification email. Please try again.");
        }
    }
}
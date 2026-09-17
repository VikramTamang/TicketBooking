package com.example.ticketbooking.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Development-only "email" delivery: logs the OTP to the console instead
 * of sending a real email, so the full login -> OTP -> verify flow can be
 * tested locally without SMTP credentials.
 *
 * This is NEVER used in prod (see @Profile) and the OTP is NEVER exposed
 * via any HTTP response — only written to this dedicated log line, which
 * only you can see on your own machine's console.
 */
@Service
@Profile({"dev", "default"})
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger("EMAIL_DEV_SIMULATION");

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, long expiryMinutes) {
        log.info("=== [DEV EMAIL SIMULATION] === To: {} | OTP: {} | Expires in {} minute(s). "
                        + "This code is NOT sent via any API response - check this console log only.",
                toEmail, otpCode, expiryMinutes);
    }
}
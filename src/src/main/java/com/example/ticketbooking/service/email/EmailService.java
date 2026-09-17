package com.example.ticketbooking.service.email;

public interface EmailService {

    void sendOtpEmail(String toEmail, String otpCode, long expiryMinutes);
}
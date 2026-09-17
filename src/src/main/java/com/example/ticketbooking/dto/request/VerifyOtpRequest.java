package com.example.ticketbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank(message = "pendingAuthToken is required")
    private String pendingAuthToken;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits")
    private String otpCode;

    public String getPendingAuthToken() {
        return pendingAuthToken;
    }

    public void setPendingAuthToken(String pendingAuthToken) {
        this.pendingAuthToken = pendingAuthToken;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
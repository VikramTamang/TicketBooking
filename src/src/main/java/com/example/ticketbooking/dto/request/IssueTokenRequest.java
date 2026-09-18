package com.example.ticketbooking.dto.request;

import jakarta.validation.constraints.NotBlank;

public class IssueTokenRequest {

    @NotBlank(message = "pendingAuthToken is required")
    private String pendingAuthToken;

    public String getPendingAuthToken() {
        return pendingAuthToken;
    }

    public void setPendingAuthToken(String pendingAuthToken) {
        this.pendingAuthToken = pendingAuthToken;
    }
}
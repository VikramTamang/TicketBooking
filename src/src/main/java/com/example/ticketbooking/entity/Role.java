package com.example.ticketbooking.entity;

/**
 * Exactly two roles, per spec. Only USER can be assigned at registration;
 * ADMIN accounts are never created through the public registration endpoint
 * (prevents privilege escalation via request payload tampering).
 */
public enum Role {
    USER,
    ADMIN
}
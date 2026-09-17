package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.PendingAuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingAuthSessionRepository extends JpaRepository<PendingAuthSession, String> {

    Optional<PendingAuthSession> findByTokenAndUsedFalse(String token);
}
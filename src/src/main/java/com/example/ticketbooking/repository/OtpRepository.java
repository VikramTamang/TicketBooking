package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByPendingAuthSession_Token(String sessionToken);
}
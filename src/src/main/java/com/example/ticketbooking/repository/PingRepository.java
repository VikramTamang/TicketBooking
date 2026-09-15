package com.example.ticketbooking.repository;

import com.example.ticketbooking.entity.Ping;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Temporary repository for Phase 2 DB connectivity verification only.
 */
public interface PingRepository extends JpaRepository<Ping, Long> {
}
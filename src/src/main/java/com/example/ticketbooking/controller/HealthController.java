package com.example.ticketbooking.controller;

import com.example.ticketbooking.entity.Ping;
import com.example.ticketbooking.repository.PingRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

    private final PingRepository pingRepository;

    public HealthController(PingRepository pingRepository) {
        this.pingRepository = pingRepository;
    }

    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    /**
     * Temporary Phase 2 endpoint: writes a row and reads it back to prove
     * the JPA/MySQL connection works end-to-end. Removed after this phase.
     */
    @GetMapping("/api/v1/db-check")
    public Map<String, Object> dbCheck() {
        pingRepository.save(new Ping("db-check-" + Instant.now()));
        List<Ping> all = pingRepository.findAll();
        return Map.of(
                "status", "DB_CONNECTED",
                "rowCount", all.size(),
                "latestMessages", all.stream().map(Ping::getMessage).toList()
        );
    }

}
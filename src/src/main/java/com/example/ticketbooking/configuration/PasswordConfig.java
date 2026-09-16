package com.example.ticketbooking.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    /**
     * BCrypt "strength" is the log2 cost factor: each +1 roughly doubles
     * hashing time. 10 (the library default) takes ~60-100ms on typical
     * hardware. 12 is a common production baseline (~250-400ms).
     *
     * Trade-off: higher strength means a brute-force/credential-stuffing
     * attacker's per-guess cost goes up proportionally, but so does the
     * CPU cost of every legitimate login/registration. This is made
     * configurable (default 10 for dev responsiveness) so production
     * config (Phase 37) can raise it without a code change.
     */
    @Value("${security.password.bcrypt-strength:10}")
    private int bcryptStrength;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
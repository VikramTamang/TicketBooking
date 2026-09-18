package com.example.ticketbooking.security;

import com.example.ticketbooking.entity.AccountStatus;
import com.example.ticketbooking.entity.User;
import com.example.ticketbooking.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Runs once per request. Extracts a Bearer token, validates its signature
 * and expiry via JwtService, then re-loads the User from the DB by the
 * id in the token's subject claim.
 *
 * Re-loading from the DB (rather than trusting the token's claims alone)
 * is what lets a DISABLED account's existing, still-unexpired token stop
 * working immediately - a purely claims-based filter could not do this.
 * See Phase 7 notes for the trade-off (one extra query per request).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());

        Optional<Claims> claimsOpt = jwtService.parseAndValidate(token);
        if (claimsOpt.isEmpty()) {
            filterChain.doFilter(request, response); // leave unauthenticated; entry point handles the 401
            return;
        }

        Claims claims = claimsOpt.get();
        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getAccountStatus() != AccountStatus.ACTIVE) {
            // Token is cryptographically valid but the account no longer
            // is - e.g. disabled after the token was issued.
            filterChain.doFilter(request, response);
            return;
        }

        User user = userOpt.get();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
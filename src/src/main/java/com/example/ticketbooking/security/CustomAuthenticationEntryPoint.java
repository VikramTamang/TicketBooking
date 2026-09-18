package com.example.ticketbooking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles requests to protected endpoints with a missing/invalid/expired
 * token. This runs at the Spring Security filter level, BEFORE any
 * controller (and therefore before GlobalExceptionHandler) is reached -
 * so it needs its own JSON error formatting to keep the response shape
 * consistent with the rest of the API.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Authentication required or token is invalid/expired");
        body.put("errorCode", "UNAUTHENTICATED");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
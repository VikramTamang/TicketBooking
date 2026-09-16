package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.request.RegisterRequest;
import com.example.ticketbooking.dto.response.UserResponse;
import com.example.ticketbooking.entity.AccountStatus;
import com.example.ticketbooking.entity.Role;
import com.example.ticketbooking.entity.User;
import com.example.ticketbooking.exception.DuplicateEmailException;
import com.example.ticketbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String hashedPassword = passwordService.hash(request.getPassword());

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                request.getPhoneNumber().trim(),
                Role.USER,
                AccountStatus.ACTIVE
        );

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }
}
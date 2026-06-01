package com.shopsphere.auth_service.service;

import com.shopsphere.auth_service.dto.request.LoginRequest;
import com.shopsphere.auth_service.dto.request.RegisterRequest;
import com.shopsphere.auth_service.dto.response.AuthResponse;
import com.shopsphere.auth_service.exception.AccountLockedException;
import com.shopsphere.auth_service.exception.EmailAlreadyExistsException;
import com.shopsphere.auth_service.exception.InvalidCredentialsException;
import com.shopsphere.auth_service.model.User;
import com.shopsphere.auth_service.model.UserStatus;
import com.shopsphere.auth_service.repository.UserRepository;
import com.shopsphere.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // Injects standard logging for security auditing
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional // Ensures the registration doesn't partially fail
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // 1. Fail Fast: Check for duplicates efficiently
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        // 2. Build the new User Entity
        User user = User.builder()
                .email(request.getEmail().toLowerCase()) // Normalize emails to lowercase
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName()) // Map the new field!
                .role(request.getRole())
                // Status is handled automatically by @PrePersist in the Entity
                .build();

        // 3. Save to database
        userRepository.save(user);
        log.info("Successfully registered user ID: {}", user.getId());

        // 4. Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new AuthResponse(token, "User registered successfully");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // 1. Find the user. Notice we use a GENERIC error message here to prevent enumeration!
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Login failed: Unknown email address: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password.");
                });

        // 2. Verify Password (Generic error message again)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Incorrect password for user ID: {}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        // 3. SECURITY CHECK: Ensure the account is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login blocked: User ID {} is suspended.", user.getId());
            throw new AccountLockedException("Your account has been suspended. Contact support.");
        }

        // 4. Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        log.info("Successful login for user ID: {}", user.getId());

        return new AuthResponse(token, "Login successful");
    }
}
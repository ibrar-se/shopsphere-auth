package com.shopsphere.auth_service.controller;

import com.shopsphere.auth_service.dto.response.ApiResponse;
import com.shopsphere.auth_service.dto.response.AuthResponse;
import com.shopsphere.auth_service.dto.request.LoginRequest;
import com.shopsphere.auth_service.dto.request.RegisterRequest;
import com.shopsphere.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
// FIX: Added /api prefix so it matches the Gateway routing and Security Config exactly!
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("REST Request received to register a new user: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("REST Request received to login user: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
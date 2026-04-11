package com.msj.controller;

import com.msj.application.service.UserService;
import com.msj.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles user login and token generation
 * Following SOLID: Single Responsibility
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthenticationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    /**
     * Login endpoint - generates JWT token
     *
     * Note: This is a simplified version for demonstration.
     * In production, integrate with proper user service and database.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            // Load user from database
            com.msj.domain.user.User user = userService.getUserByUsername(request.username());

            // Check if account is enabled
            if (!user.isEnabled()) {
                log.warn("Login attempt for disabled account: {}", request.username());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                log.warn("Login attempt for locked account: {}", request.username());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verify password
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                log.warn("Invalid password for user: {}", request.username());
                userService.recordFailedLoginAttempt(request.username());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Record successful login
            userService.recordSuccessfulLogin(request.username());

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(request.username());
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.username());

            log.info("Login successful for user: {}", request.username());

            return ResponseEntity.ok(new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                3600  // 1 hour in seconds
            ));
        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Authentication error for user: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh token endpoint
     * Generates new access token using refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        log.debug("Refreshing token");

        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            log.warn("Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = jwtTokenProvider.getUsernameFromToken(request.refreshToken());
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        return ResponseEntity.ok(new RefreshResponse(
            newAccessToken,
            "Bearer",
            3600
        ));
    }

    /**
     * Logout endpoint (optional - primarily client-side)
     * Can be used to invalidate tokens on server-side token blacklist
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (clear client-side token)")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout endpoint called");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");

        return ResponseEntity.ok(response);
    }

    /**
     * Login Request DTO
     */
    public record LoginRequest(
        String username,
        String password
    ) {}

    /**
     * Login Response DTO
     */
    public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn
    ) {}

    /**
     * Refresh Request DTO
     */
    public record RefreshRequest(
        String refreshToken
    ) {}

    /**
     * Refresh Response DTO
     */
    public record RefreshResponse(
        String accessToken,
        String tokenType,
        int expiresIn
    ) {}
}


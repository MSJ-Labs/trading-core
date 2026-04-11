package com.msj.controller;

import com.msj.application.service.UserService;
import com.msj.controller.dto.*;
import com.msj.controller.mapper.UserMapper;
import com.msj.domain.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for User management endpoints
 * Following SOLID: Single Responsibility (user HTTP operations)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management and profile endpoints")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user account")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // For demo, assign default USER role
        // In production, you might want to assign different roles based on registration type
        Role defaultRole = Role.builder()
                .id(RoleId.of("550e8400-e29b-41d4-a716-446655440002")) // ROLE_USER
                .name("ROLE_USER")
                .description("Regular user")
                .build();

        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                Set.of(defaultRole)
        );

        log.info("User registered successfully with id: {}", user.getId().value());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserResponse(user));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Fetching profile for user: {}", username);

        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(userMapper.toUserProfileResponse(user));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update current user's profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        String username = authentication.getName();
        log.info("Updating profile for user: {}", username);

        User user = userService.getUserByUsername(username);
        UserProfile profile = userService.updateUserProfile(
                user.getId(),
                request.getTimezone(),
                request.getLanguage(),
                request.getTheme(),
                request.getNotificationsEnabled(),
                request.getTwoFactorEnabled()
        );

        User updatedUser = userService.getUserById(user.getId());
        return ResponseEntity.ok(userMapper.toUserProfileResponse(updatedUser));
    }

    /**
     * Change current user password
     */
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change current user's password")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        log.info("Changing password for user: {}", username);

        User user = userService.getUserByUsername(username);
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Fetching all users");

        // TODO: Implement pagination for production
        // For now, return empty list as we don't have a method to get all users
        // In production, you'd add this to UserRepository and UserService

        return ResponseEntity.ok(List.of());
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user by ID (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.debug("Fetching user with id: {}", id);

        User user = userService.getUserById(UserId.of(id));
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    /**
     * Update user (Admin only)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userService.updateUser(
                UserId.of(id),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    /**
     * Enable/disable user (Admin only)
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Enable/disable user", description = "Enable or disable user account (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> setUserStatus(
            @PathVariable String id,
            @RequestBody SetUserStatusRequest request) {
        log.info("{} user account: {}", request.getEnabled() ? "Enabling" : "Disabling", id);

        User user = userService.setUserEnabled(UserId.of(id), request.getEnabled());
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user account (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Deleting user with id: {}", id);

        userService.deleteUser(UserId.of(id));
        return ResponseEntity.noContent().build();
    }
}

package com.msj.application.service;

import com.msj.domain.user.*;
import com.msj.infrastructure.ports.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Application service for User operations
 * Implements UserDetailsService for Spring Security integration
 * Following SOLID: Single Responsibility (user business logic)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String rawPassword,
                          String firstName, String lastName, Set<Role> roles) {
        log.info("Creating user: {}", username);

        // Validate uniqueness
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create user
        User user = User.create(username, email, encodedPassword, firstName, lastName);
        user.setRoles(roles);

        // Create default profile
        UserProfile profile = UserProfile.create(user.getId());
        user.setProfile(profile);

        User saved = userRepository.save(user);
        log.info("User created successfully with id: {}", saved.getId().value());

        return saved;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(UserId id) {
        log.debug("Fetching user with id: {}", id.value());
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id.value()));
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Update user information
     */
    public User updateUser(UserId id, String firstName, String lastName, String email) {
        log.info("Updating user with id: {}", id.value());

        User user = getUserById(id);

        // Check email uniqueness if changed
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }
        }

        user.update(firstName, lastName, email);
        User saved = userRepository.save(user);

        log.info("User updated successfully");
        return saved;
    }

    /**
     * Change user password
     */
    public void changePassword(UserId id, String currentPassword, String newPassword) {
        log.info("Changing password for user id: {}", id.value());

        User user = getUserById(id);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Encode and set new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);

        userRepository.save(user);
        log.info("Password changed successfully for user: {}", id.value());
    }

    /**
     * Assign roles to user
     */
    public User assignRoles(UserId userId, Set<Role> roles) {
        log.info("Assigning roles to user: {}", userId.value());

        User user = getUserById(userId);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        log.info("Roles assigned successfully");

        return saved;
    }

    /**
     * Enable/disable user account
     */
    public User setUserEnabled(UserId id, boolean enabled) {
        log.info("{} user account: {}", enabled ? "Enabling" : "Disabling", id.value());

        User user = getUserById(id);
        user.setEnabled(enabled);

        User saved = userRepository.save(user);
        log.info("User account {} successfully", enabled ? "enabled" : "disabled");

        return saved;
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String username) {
        try {
            User user = getUserByUsername(username);
            user.recordSuccessfulLogin();
            userRepository.save(user);
            log.debug("Successful login recorded for user: {}", username);
        } catch (Exception e) {
            log.warn("Could not record successful login for user: {}", username, e);
        }
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLoginAttempt(String username) {
        try {
            User user = getUserByUsername(username);
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            log.debug("Failed login attempt recorded for user: {}", username);
        } catch (Exception e) {
            log.warn("Could not record failed login attempt for user: {}", username, e);
        }
    }

    /**
     * Delete user
     */
    public void deleteUser(UserId id) {
        log.info("Deleting user with id: {}", id.value());

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id.value());
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully");
    }

    /**
     * Update user profile
     */
    public UserProfile updateUserProfile(UserId userId, String timezone, String language,
                                       String theme, Boolean notificationsEnabled,
                                       Boolean twoFactorEnabled) {
        log.info("Updating profile for user: {}", userId.value());

        User user = getUserById(userId);

        if (user.getProfile() == null) {
            user.setProfile(UserProfile.create(userId));
        }

        user.getProfile().update(timezone, language, theme, notificationsEnabled, twoFactorEnabled);
        userRepository.save(user);

        log.info("User profile updated successfully");
        return user.getProfile();
    }

    // Spring Security UserDetailsService implementation

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }

    /**
     * Check if user has permission
     */
    public boolean hasPermission(String username, String permission) {
        try {
            User user = getUserByUsername(username);
            return user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user has role
     */
    public boolean hasRole(String username, String role) {
        try {
            User user = getUserByUsername(username);
            return user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
        } catch (Exception e) {
            return false;
        }
    }
}

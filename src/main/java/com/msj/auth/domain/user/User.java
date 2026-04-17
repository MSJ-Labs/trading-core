package com.msj.auth.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User aggregate root — pure domain object, zero Spring/infrastructure dependencies.
 * Business rules live here: account locking, login tracking.
 */
@Getter
@Builder
public class User {

    private final UserId id;
    private final String username;
    private final String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;

    // Role names loaded by repository (e.g. "ROLE_USER", "ROLE_ADMIN")
    private Set<String> roles;

    public static User register(String username, String email, String passwordHash,
                                String firstName, String lastName) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password is required");

        return User.builder()
                .id(UserId.generate())
                .username(username.trim())
                .email(email.trim().toLowerCase())
                .passwordHash(passwordHash)
                .firstName(firstName != null ? firstName.trim() : null)
                .lastName(lastName != null ? lastName.trim() : null)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .failedLoginAttempts(0)
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Auto-unlocks if the lock period has passed.
     */
    public boolean isCurrentlyLocked() {
        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            this.accountNonLocked = true;
            this.lockedUntil = null;
            this.failedLoginAttempts = 0;
        }
        return !accountNonLocked;
    }

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) throw new IllegalArgumentException("Password is required");
        this.passwordHash = newPasswordHash;
        this.credentialsNonExpired = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void withRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getFullName() {
        if (firstName != null && lastName != null) return firstName + " " + lastName;
        if (firstName != null) return firstName;
        if (lastName != null) return lastName;
        return username;
    }
}
package com.msj.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User entity implementing Spring Security UserDetails
 * Following SOLID: Single Responsibility (user domain logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    private UserId id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;

    // Relationships
    private Set<Role> roles;
    private UserProfile profile;

    /**
     * Factory method to create a new user
     */
    public static User create(String username, String email, String password,
                             String firstName, String lastName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        User user = User.builder()
                .id(UserId.generate())
                .username(username.trim())
                .email(email.trim().toLowerCase())
                .password(password)
                .firstName(firstName != null ? firstName.trim() : null)
                .lastName(lastName != null ? lastName.trim() : null)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .build();

        return user;
    }

    /**
     * Update user information
     */
    public void update(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email.trim().toLowerCase();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Change password
     */
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        this.password = newPassword;
        this.credentialsNonExpired = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true;
        this.lockedUntil = null;
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts = (this.failedLoginAttempts != null ? this.failedLoginAttempts : 0) + 1;

        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30); // Lock for 30 minutes
        }
    }

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked() {
        if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return true;
        }
        // Unlock if lock period has expired
        if (!accountNonLocked && lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            this.accountNonLocked = true;
            this.lockedUntil = null;
            this.failedLoginAttempts = 0;
        }
        return !accountNonLocked;
    }

    /**
     * Enable/disable user account
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    // Spring Security UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

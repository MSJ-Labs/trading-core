package com.msj.auth.support;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Central factory for building test User instances.
 * Use static imports in tests for readability.
 */
public final class UserTestFactory {

    private UserTestFactory() {}

    public static User activeUser() {
        return activeUser("jdoe");
    }

    public static User activeUser(String username) {
        return User.builder()
                .id(UserId.generate())
                .username(username)
                .email(username + "@example.com")
                .passwordHash("$hashed$")
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

    public static User lockedUser() {
        return lockedUser("locked");
    }

    public static User lockedUser(String username) {
        return User.builder()
                .id(UserId.generate())
                .username(username)
                .email(username + "@example.com")
                .passwordHash("$hashed$")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(false)
                .credentialsNonExpired(true)
                .failedLoginAttempts(5)
                .lockedUntil(LocalDateTime.now().plusMinutes(25))
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User expiredLockUser(String username) {
        return User.builder()
                .id(UserId.generate())
                .username(username)
                .email(username + "@example.com")
                .passwordHash("$hashed$")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(false)
                .credentialsNonExpired(true)
                .failedLoginAttempts(5)
                .lockedUntil(LocalDateTime.now().minusMinutes(1))
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
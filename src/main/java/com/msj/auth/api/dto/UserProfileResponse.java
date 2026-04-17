package com.msj.auth.api.dto;

import com.msj.auth.domain.user.User;

import java.time.LocalDateTime;
import java.util.Set;

public record UserProfileResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String fullName,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId().value().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
package com.msj.domain.user;

import java.util.UUID;

/**
 * Value object for UserProfile ID
 */
public record UserProfileId(String value) {

    public UserProfileId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserProfileId value cannot be null or blank");
        }
    }

    public static UserProfileId generate() {
        return new UserProfileId(UUID.randomUUID().toString());
    }

    public static UserProfileId of(String value) {
        return new UserProfileId(value);
    }
}

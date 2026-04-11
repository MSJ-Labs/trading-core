package com.msj.domain.user;

import java.util.UUID;

/**
 * Value object for Role ID
 */
public record RoleId(String value) {

    public RoleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RoleId value cannot be null or blank");
        }
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID().toString());
    }

    public static RoleId of(String value) {
        return new RoleId(value);
    }
}

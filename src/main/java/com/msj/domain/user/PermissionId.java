package com.msj.domain.user;

import java.util.UUID;

/**
 * Value object for Permission ID
 */
public record PermissionId(String value) {

    public PermissionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PermissionId value cannot be null or blank");
        }
    }

    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID().toString());
    }

    public static PermissionId of(String value) {
        return new PermissionId(value);
    }
}

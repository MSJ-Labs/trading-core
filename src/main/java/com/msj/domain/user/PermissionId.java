package com.msj.domain.user;

import io.hypersistence.tsid.TSID;

/**
 * Value object for Permission ID
 */
public record PermissionId(TSID value) {

    public PermissionId {
        if (value == null) {
            throw new IllegalArgumentException("PermissionId value cannot be null");
        }
    }

    public static PermissionId generate() {
        return new PermissionId(TSID.fast());
    }

    public static PermissionId of(String value) {
        return new PermissionId(TSID.from(value));
    }

    public static PermissionId of(long value) {
        return new PermissionId(TSID.from(value));
    }
}

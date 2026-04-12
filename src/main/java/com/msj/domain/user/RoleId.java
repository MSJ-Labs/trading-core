package com.msj.domain.user;

import io.hypersistence.tsid.TSID;

/**
 * Value object for Role ID
 */
public record RoleId(TSID value) {

    public RoleId {
        if (value == null) {
            throw new IllegalArgumentException("RoleId value cannot be null");
        }
    }

    public static RoleId generate() {
        return new RoleId(TSID.fast());
    }

    public static RoleId of(String value) {
        return new RoleId(TSID.from(value));
    }

    public static RoleId of(long value) {
        return new RoleId(TSID.from(value));
    }
}

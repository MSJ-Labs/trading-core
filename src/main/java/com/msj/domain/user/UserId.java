package com.msj.domain.user;

import io.hypersistence.tsid.TSID;

/**
 * Value object for User ID
 */
public record UserId(TSID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(TSID.fast());
    }

    public static UserId of(String value) {
        return new UserId(TSID.from(value));
    }

    public static UserId of(long value) {
        return new UserId(TSID.from(value));
    }
}

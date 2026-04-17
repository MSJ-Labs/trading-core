package com.msj.auth.domain.user;

import io.hypersistence.tsid.TSID;

public record UserId(TSID value) {

    public UserId {
        if (value == null) throw new IllegalArgumentException("UserId cannot be null");
    }

    public static UserId generate() {
        return new UserId(TSID.fast());
    }

    public static UserId of(long value) {
        return new UserId(TSID.from(value));
    }

    public static UserId of(String value) {
        return new UserId(TSID.from(value));
    }
}
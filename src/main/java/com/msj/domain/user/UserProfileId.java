package com.msj.domain.user;

import io.hypersistence.tsid.TSID;

/**
 * Value object for UserProfile ID
 */
public record UserProfileId(TSID value) {

    public UserProfileId {
        if (value == null) {
            throw new IllegalArgumentException("UserProfileId value cannot be null");
        }
    }

    public static UserProfileId generate() {
        return new UserProfileId(TSID.fast());
    }

    public static UserProfileId of(String value) {
        return new UserProfileId(TSID.from(value));
    }

    public static UserProfileId of(long value) {
        return new UserProfileId(TSID.from(value));
    }
}

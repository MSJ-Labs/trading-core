package com.msj.domain.crypto;

import io.hypersistence.tsid.TSID;

/**
 * Value object representing a Crypto entity ID
 */
public record CryptoId(TSID value) {

    public CryptoId {
        if (value == null) {
            throw new IllegalArgumentException("CryptoId value cannot be null");
        }
    }

    public static CryptoId generate() {
        return new CryptoId(TSID.fast());
    }

    public static CryptoId of(String value) {
        return new CryptoId(TSID.from(value));
    }

    public static CryptoId of(long value) {
        return new CryptoId(TSID.from(value));
    }
}

package com.msj.domain.crypto;

import java.util.UUID;

/**
 * Value object representing a Crypto entity ID
 */
public record CryptoId(String value) {

    public CryptoId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CryptoId value cannot be null or blank");
        }
    }

    public static CryptoId generate() {
        return new CryptoId(UUID.randomUUID().toString());
    }

    public static CryptoId of(String value) {
        return new CryptoId(value);
    }
}


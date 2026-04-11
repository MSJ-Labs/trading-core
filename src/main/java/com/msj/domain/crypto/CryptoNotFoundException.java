package com.msj.domain.crypto;

/**
 * Exception thrown when Crypto is not found
 */
public class CryptoNotFoundException extends RuntimeException {

    public CryptoNotFoundException(String message) {
        super(message);
    }

    public CryptoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CryptoNotFoundException forId(CryptoId id) {
        return new CryptoNotFoundException("Crypto not found with id: " + id.value());
    }
}


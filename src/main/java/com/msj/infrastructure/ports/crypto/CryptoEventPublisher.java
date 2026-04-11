package com.msj.infrastructure.ports.crypto;

import com.msj.domain.crypto.Crypto;

/**
 * Port interface for publishing crypto events
 */
public interface CryptoEventPublisher {

    /**
     * Publish crypto created event
     */
    void publishCryptoCreated(Crypto crypto);

    /**
     * Publish crypto updated event
     */
    void publishCryptoUpdated(Crypto crypto);

    /**
     * Publish crypto deleted event
     */
    void publishCryptoDeleted(String cryptoId);
}


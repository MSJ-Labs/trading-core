package com.msj.infrastructure.ports.crypto;

import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for Crypto persistence
 */
public interface CryptoRepository {

    /**
     * Save or update a crypto asset
     */
    Crypto save(Crypto crypto);

    /**
     * Find crypto by ID
     */
    Optional<Crypto> findById(CryptoId id);

    /**
     * Find crypto by symbol
     */
    Optional<Crypto> findBySymbol(String symbol);

    /**
     * Find all cryptos
     */
    List<Crypto> findAll();

    /**
     * Delete crypto by ID
     */
    void deleteById(CryptoId id);

    /**
     * Check if crypto exists by ID
     */
    boolean existsById(CryptoId id);
}


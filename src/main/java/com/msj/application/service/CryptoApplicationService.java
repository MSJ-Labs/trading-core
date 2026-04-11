package com.msj.application.service;

import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import com.msj.domain.crypto.CryptoNotFoundException;
import com.msj.infrastructure.ports.crypto.CryptoRepository;
import com.msj.infrastructure.ports.crypto.CryptoEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service for Crypto operations using hexagonal architecture
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CryptoApplicationService {

    private final CryptoRepository cryptoRepository;
    private final CryptoEventPublisher eventPublisher;

    /**
     * Create a new crypto asset
     */
    public Crypto createCrypto(String symbol, String name, BigDecimal currentPrice,
                               BigDecimal marketCap, BigDecimal volume24h,
                               BigDecimal changePercent24h, String description) {
        log.info("Creating crypto: {}", symbol);

        Crypto crypto = Crypto.create(symbol, name, currentPrice, marketCap, volume24h,
                                      changePercent24h, description);
        Crypto saved = cryptoRepository.save(crypto);

        eventPublisher.publishCryptoCreated(saved);
        log.info("Crypto created successfully with id: {}", saved.getId().value());

        return saved;
    }

    /**
     * Get crypto by ID
     */
    @Transactional(readOnly = true)
    public Crypto getCryptoById(CryptoId id) {
        log.info("Fetching crypto with id: {}", id.value());
        return cryptoRepository.findById(id)
                .orElseThrow(() -> CryptoNotFoundException.forId(id));
    }

    /**
     * Get crypto by symbol
     */
    @Transactional(readOnly = true)
    public Crypto getCryptoBySymbol(String symbol) {
        log.info("Fetching crypto with symbol: {}", symbol);
        return cryptoRepository.findBySymbol(symbol)
                .orElseThrow(() -> new CryptoNotFoundException("Crypto not found with symbol: " + symbol));
    }

    /**
     * Get all cryptos
     */
    @Transactional(readOnly = true)
    public List<Crypto> getAllCryptos() {
        log.info("Fetching all cryptos");
        return cryptoRepository.findAll();
    }

    /**
     * Update crypto
     */
    public Crypto updateCrypto(CryptoId id, String name, BigDecimal currentPrice,
                              BigDecimal marketCap, BigDecimal volume24h,
                              BigDecimal changePercent24h, String description) {
        log.info("Updating crypto with id: {}", id.value());

        Crypto crypto = cryptoRepository.findById(id)
                .orElseThrow(() -> CryptoNotFoundException.forId(id));

        crypto.update(name, currentPrice, marketCap, volume24h, changePercent24h, description);
        Crypto updated = cryptoRepository.save(crypto);

        eventPublisher.publishCryptoUpdated(updated);
        log.info("Crypto updated successfully");

        return updated;
    }

    /**
     * Delete crypto
     */
    public void deleteCrypto(CryptoId id) {
        log.info("Deleting crypto with id: {}", id.value());

        if (!cryptoRepository.existsById(id)) {
            throw CryptoNotFoundException.forId(id);
        }

        cryptoRepository.deleteById(id);
        eventPublisher.publishCryptoDeleted(id.value());
        log.info("Crypto deleted successfully");
    }
}


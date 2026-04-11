package com.msj.domain.crypto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Crypto aggregate root representing a cryptocurrency asset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crypto {

    private CryptoId id;
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal changePercent24h;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a new Crypto entity
     */
    public static Crypto create(String symbol, String name, BigDecimal currentPrice,
                                BigDecimal marketCap, BigDecimal volume24h,
                                BigDecimal changePercent24h, String description) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        Crypto crypto = new Crypto();
        crypto.id = CryptoId.generate();
        crypto.symbol = symbol;
        crypto.name = name;
        crypto.currentPrice = currentPrice;
        crypto.marketCap = marketCap;
        crypto.volume24h = volume24h;
        crypto.changePercent24h = changePercent24h;
        crypto.description = description;
        crypto.createdAt = LocalDateTime.now();
        crypto.updatedAt = LocalDateTime.now();

        return crypto;
    }

    /**
     * Update crypto information
     */
    public void update(String name, BigDecimal currentPrice, BigDecimal marketCap,
                       BigDecimal volume24h, BigDecimal changePercent24h, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (currentPrice != null) {
            this.currentPrice = currentPrice;
        }
        if (marketCap != null) {
            this.marketCap = marketCap;
        }
        if (volume24h != null) {
            this.volume24h = volume24h;
        }
        if (changePercent24h != null) {
            this.changePercent24h = changePercent24h;
        }
        if (description != null && !description.isBlank()) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }
}

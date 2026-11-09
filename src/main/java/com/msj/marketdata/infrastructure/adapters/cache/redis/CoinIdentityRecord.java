package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;

/**
 * Stable part of a coin (no price data). Stored without TTL so a ranked coin never disappears
 * from the top list when its short-lived price snapshot expires.
 */
record CoinIdentityRecord(String coinId, String symbol, String name, String imageUrl) {

    static CoinIdentityRecord fromDomain(CoinPrice coinPrice) {
        return new CoinIdentityRecord(coinPrice.coinId(), coinPrice.symbol(), coinPrice.name(), coinPrice.imageUrl());
    }

    /** Coin without any price field: used when the price snapshot has expired. */
    CoinPrice toDomainWithoutPrice() {
        return CoinPrice.builder()
                .id(coinId)
                .symbol(symbol)
                .name(name)
                .imageUrl(imageUrl)
                .build();
    }
}
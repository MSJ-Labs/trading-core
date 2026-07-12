package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;

import java.math.BigDecimal;
import java.time.Instant;

record CoinPriceRecord(
        String coinId,
        String symbol,
        String name,
        BigDecimal priceUsd,
        BigDecimal priceChangePercent24h,
        BigDecimal marketCapUsd,
        BigDecimal volume24h,
        Instant lastUpdated,
        String imageUrl
) {

    static CoinPriceRecord fromDomain(CoinPrice coinPrice) {
        return new CoinPriceRecord(
                coinPrice.coinId(),
                coinPrice.symbol(),
                coinPrice.name(),
                coinPrice.priceUsd(),
                coinPrice.priceChangePercent24h(),
                coinPrice.marketCapUsd(),
                coinPrice.volume24h(),
                coinPrice.lastUpdated(),
                coinPrice.imageUrl());
    }

    CoinPrice toDomain() {
        return CoinPrice.builder()
                .id(coinId)
                .symbol(symbol)
                .name(name)
                .priceUsd(priceUsd)
                .priceChangePercent24h(priceChangePercent24h)
                .marketCapUsd(marketCapUsd)
                .volume24h(volume24h)
                .lastUpdated(lastUpdated)
                .imageUrl(imageUrl)
                .build();
    }
}
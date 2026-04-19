package com.msj.marketdata.api.dto;

import com.msj.marketdata.domain.CoinPrice;

import java.math.BigDecimal;
import java.time.Instant;

public record CoinPriceResponse(
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
    public static CoinPriceResponse from(CoinPrice coinPrice) {
        return new CoinPriceResponse(
                coinPrice.coinId(),
                coinPrice.symbol(),
                coinPrice.name(),
                coinPrice.priceUsd(),
                coinPrice.priceChangePercent24h(),
                coinPrice.marketCapUsd(),
                coinPrice.volume24h(),
                coinPrice.lastUpdated(),
                coinPrice.imageUrl()
        );
    }
}

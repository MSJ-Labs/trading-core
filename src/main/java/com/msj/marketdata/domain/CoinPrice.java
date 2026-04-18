package com.msj.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CoinPrice(
        String coinId,
        String symbol,
        String name,
        BigDecimal priceUsd,
        BigDecimal priceChangePercent24h,
        BigDecimal marketCapUsd,
        BigDecimal volume24h,
        Instant lastUpdated
) {}
package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CoinPriceRecordTest {

    @Test
    void fromDomain_then_toDomain_round_trips_all_fields() {
        CoinPrice original = CoinPrice.builder()
                .id("bitcoin")
                .symbol("BTC")
                .name("Bitcoin")
                .priceUsd(BigDecimal.valueOf(50_000))
                .priceChangePercent24h(BigDecimal.valueOf(1.5))
                .marketCapUsd(BigDecimal.valueOf(1_000_000_000))
                .volume24h(BigDecimal.valueOf(20_000_000))
                .lastUpdated(Instant.parse("2026-07-11T00:00:00Z"))
                .imageUrl("https://example.com/btc.png")
                .build();

        CoinPrice roundTripped = CoinPriceRecord.fromDomain(original).toDomain();

        assertThat(roundTripped.coinId()).isEqualTo(original.coinId());
        assertThat(roundTripped.symbol()).isEqualTo(original.symbol());
        assertThat(roundTripped.name()).isEqualTo(original.name());
        assertThat(roundTripped.priceUsd()).isEqualByComparingTo(original.priceUsd());
        assertThat(roundTripped.priceChangePercent24h()).isEqualByComparingTo(original.priceChangePercent24h());
        assertThat(roundTripped.marketCapUsd()).isEqualByComparingTo(original.marketCapUsd());
        assertThat(roundTripped.volume24h()).isEqualByComparingTo(original.volume24h());
        assertThat(roundTripped.lastUpdated()).isEqualTo(original.lastUpdated());
        assertThat(roundTripped.imageUrl()).isEqualTo(original.imageUrl());
    }
}
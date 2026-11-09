package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CoinIdentityRecordTest {

    @Test
    void fromDomain_then_toDomainWithoutPrice_keeps_identity_and_drops_price_fields() {
        CoinPrice original = CoinPrice.builder()
                .id("bitcoin").symbol("BTC").name("Bitcoin")
                .priceUsd(BigDecimal.valueOf(50_000))
                .priceChangePercent24h(BigDecimal.valueOf(1.5))
                .marketCapUsd(BigDecimal.valueOf(1_000_000_000))
                .volume24h(BigDecimal.valueOf(20_000_000))
                .lastUpdated(Instant.parse("2026-07-11T00:00:00Z"))
                .imageUrl("https://example.com/btc.png")
                .build();

        CoinPrice result = CoinIdentityRecord.fromDomain(original).toDomainWithoutPrice();

        assertThat(result.coinId()).isEqualTo("bitcoin");
        assertThat(result.symbol()).isEqualTo("BTC");
        assertThat(result.name()).isEqualTo("Bitcoin");
        assertThat(result.imageUrl()).isEqualTo("https://example.com/btc.png");
        assertThat(result.priceUsd()).isNull();
        assertThat(result.priceChangePercent24h()).isNull();
        assertThat(result.marketCapUsd()).isNull();
        assertThat(result.volume24h()).isNull();
        assertThat(result.lastUpdated()).isNull();
    }
}
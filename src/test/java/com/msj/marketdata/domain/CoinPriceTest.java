package com.msj.marketdata.domain;

import com.msj.marketdata.domain.events.PriceUpdatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CoinPriceTest {

    private CoinPrice bitcoin() {
        return CoinPrice.builder()
                .id("bitcoin")
                .symbol("BTC")
                .name("Bitcoin")
                .priceUsd(BigDecimal.valueOf(50_000))
                .priceChangePercent24h(BigDecimal.valueOf(2.5))
                .marketCapUsd(BigDecimal.valueOf(1_000_000_000))
                .volume24h(BigDecimal.valueOf(30_000_000))
                .lastUpdated(Instant.parse("2026-06-07T10:00:00Z"))
                .imageUrl("https://img/btc.png")
                .build();
    }

    @Test
    void accessors_return_constructor_values() {
        CoinPrice coin = bitcoin();

        assertThat(coin.coinId()).isEqualTo("bitcoin");
        assertThat(coin.symbol()).isEqualTo("BTC");
        assertThat(coin.name()).isEqualTo("Bitcoin");
        assertThat(coin.priceUsd()).isEqualByComparingTo("50000");
        assertThat(coin.priceChangePercent24h()).isEqualByComparingTo("2.5");
        assertThat(coin.marketCapUsd()).isEqualByComparingTo("1000000000");
        assertThat(coin.volume24h()).isEqualByComparingTo("30000000");
        assertThat(coin.lastUpdated()).isEqualTo(Instant.parse("2026-06-07T10:00:00Z"));
        assertThat(coin.imageUrl()).isEqualTo("https://img/btc.png");
    }

    @Test
    void withUpdatedPrice_returns_new_instance_with_updated_price_and_timestamp() {
        CoinPrice original = bitcoin();
        Instant newTs = Instant.parse("2026-06-07T11:00:00Z");
        PriceUpdate tick = new PriceUpdate("BTCUSDT", BigDecimal.valueOf(60_000), newTs);

        CoinPrice updated = original.withUpdatedPrice(tick);

        assertThat(updated.priceUsd()).isEqualByComparingTo("60000");
        assertThat(updated.lastUpdated()).isEqualTo(newTs);
        assertThat(updated.coinId()).isEqualTo("bitcoin");
        assertThat(updated.symbol()).isEqualTo("BTC");
        assertThat(updated.marketCapUsd()).isEqualByComparingTo("1000000000");
    }

    @Test
    void withUpdatedPrice_registers_domain_event() {
        Instant ts = Instant.parse("2026-06-07T11:00:00Z");
        PriceUpdate tick = new PriceUpdate("BTCUSDT", BigDecimal.valueOf(60_000), ts);

        CoinPrice updated = bitcoin().withUpdatedPrice(tick);

        assertThat(updated.pullDomainEvents())
                .containsExactly(new PriceUpdatedEvent("bitcoin", "BTC", BigDecimal.valueOf(60_000), ts));
    }

    @Test
    void withUpdatedPrice_does_not_mutate_original() {
        CoinPrice original = bitcoin();
        original.withUpdatedPrice(new PriceUpdate("BTCUSDT", BigDecimal.valueOf(60_000), Instant.now()));

        assertThat(original.priceUsd()).isEqualByComparingTo("50000");
        assertThat(original.pullDomainEvents()).isEmpty();
    }

    @Test
    void equality_is_identity_based_on_coinId() {
        CoinPrice a = bitcoin();
        CoinPrice b = CoinPrice.builder()
                .id("bitcoin").symbol("BTC").name("Bitcoin")
                .priceUsd(BigDecimal.valueOf(99_999)).priceChangePercent24h(BigDecimal.ONE)
                .marketCapUsd(BigDecimal.ONE).volume24h(BigDecimal.ONE)
                .lastUpdated(Instant.now()).imageUrl(null)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
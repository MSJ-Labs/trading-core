package com.msj.marketdata.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PriceUpdateTest {

    @Test
    void record_accessors_return_constructor_values() {
        Instant now = Instant.now();
        PriceUpdate update = new PriceUpdate("BTCUSDT", BigDecimal.valueOf(50_000), now);

        assertThat(update.symbol()).isEqualTo("BTCUSDT");
        assertThat(update.price()).isEqualByComparingTo("50000");
        assertThat(update.timestamp()).isEqualTo(now);
    }

    @Test
    void equals_and_hashCode_are_value_based() {
        Instant now = Instant.now();
        PriceUpdate a = new PriceUpdate("ETHUSDT", BigDecimal.valueOf(3_000), now);
        PriceUpdate b = new PriceUpdate("ETHUSDT", BigDecimal.valueOf(3_000), now);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}

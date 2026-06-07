package com.msj.marketdata.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OhlcvCandleTest {

    private static final Instant OPEN_TIME = Instant.parse("2026-06-07T10:00:00Z");

    @Test
    void create_sets_all_ohlc_to_first_price_and_count_to_one() {
        OhlcvCandle candle = OhlcvCandle.create("BTCUSDT", OPEN_TIME, new BigDecimal("50000"));

        assertThat(candle.symbol()).isEqualTo("BTCUSDT");
        assertThat(candle.openTime()).isEqualTo(OPEN_TIME);
        assertThat(candle.open()).isEqualByComparingTo("50000");
        assertThat(candle.high()).isEqualByComparingTo("50000");
        assertThat(candle.low()).isEqualByComparingTo("50000");
        assertThat(candle.close()).isEqualByComparingTo("50000");
        assertThat(candle.priceUpdateCount()).isEqualTo(1);
    }

    @Test
    void withTick_updates_high_when_price_exceeds_current_high() {
        OhlcvCandle candle = OhlcvCandle.create("BTCUSDT", OPEN_TIME, new BigDecimal("50000"))
                .withTick(new BigDecimal("55000"));

        assertThat(candle.high()).isEqualByComparingTo("55000");
        assertThat(candle.low()).isEqualByComparingTo("50000");
        assertThat(candle.close()).isEqualByComparingTo("55000");
        assertThat(candle.open()).isEqualByComparingTo("50000");
        assertThat(candle.priceUpdateCount()).isEqualTo(2);
    }

    @Test
    void withTick_updates_low_when_price_falls_below_current_low() {
        OhlcvCandle candle = OhlcvCandle.create("BTCUSDT", OPEN_TIME, new BigDecimal("50000"))
                .withTick(new BigDecimal("45000"));

        assertThat(candle.low()).isEqualByComparingTo("45000");
        assertThat(candle.high()).isEqualByComparingTo("50000");
        assertThat(candle.close()).isEqualByComparingTo("45000");
    }

    @Test
    void withTick_does_not_mutate_original() {
        OhlcvCandle original = OhlcvCandle.create("BTCUSDT", OPEN_TIME, new BigDecimal("50000"));
        original.withTick(new BigDecimal("99999"));

        assertThat(original.close()).isEqualByComparingTo("50000");
        assertThat(original.priceUpdateCount()).isEqualTo(1);
    }
}
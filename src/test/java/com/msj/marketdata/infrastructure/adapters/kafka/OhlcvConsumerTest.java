package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.OhlcvAggregationUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OhlcvConsumerTest {

    @Mock
    private OhlcvAggregationUseCase ohlcvAggregationUseCase;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private OhlcvConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OhlcvConsumer(ohlcvAggregationUseCase, meterRegistry);
    }

    @Test
    void consume_delegatesToAggregationUseCase() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000"), Instant.now());

        consumer.consume(tick);

        verify(ohlcvAggregationUseCase).onTick(tick);
    }

    @Test
    void handleDlt_logsWithoutThrowing_andNeverTouchesUseCase() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000"), Instant.now());

        assertThatCode(() -> consumer.handleDlt(tick, 7L, "boom")).doesNotThrowAnyException();
        verifyNoInteractions(ohlcvAggregationUseCase);
    }

    @Test
    void handleDlt_incrementsDltCounter() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000"), Instant.now());

        consumer.handleDlt(tick, 7L, "boom");

        assertThat(meterRegistry.counter("marketdata.kafka.dlt", "consumer", "ohlcv-aggregator").count())
                .isEqualTo(1.0);
    }
}
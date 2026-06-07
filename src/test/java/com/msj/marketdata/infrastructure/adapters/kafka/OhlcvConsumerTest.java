package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.OhlcvAggregationUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OhlcvConsumerTest {

    @Mock
    private OhlcvAggregationUseCase ohlcvAggregationUseCase;

    @InjectMocks
    private OhlcvConsumer consumer;

    @Test
    void consume_delegatesToAggregationUseCase() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000"), Instant.now());

        consumer.consume(tick);

        verify(ohlcvAggregationUseCase).onTick(tick);
    }
}
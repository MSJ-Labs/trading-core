package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceTickRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceTickConsumerTest {

    @Mock
    private PriceTickRepository priceTickRepository;

    @InjectMocks
    private PriceTickConsumer consumer;

    @Test
    void consume_persistsTick() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), Instant.now());

        consumer.consume(tick);

        verify(priceTickRepository).save(tick);
    }
}
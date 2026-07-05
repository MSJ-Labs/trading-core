package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.PersistPriceTickCommand;
import com.msj.marketdata.application.command.PersistPriceTickUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PriceTickConsumerTest {

    @Mock
    private PersistPriceTickUseCase persistPriceTickUseCase;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private PriceTickConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PriceTickConsumer(persistPriceTickUseCase, meterRegistry);
    }

    @Test
    void consume_delegatesToUseCase() {
        Instant ts = Instant.now();
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), ts);

        consumer.consume(tick);

        ArgumentCaptor<PersistPriceTickCommand> captor = ArgumentCaptor.forClass(PersistPriceTickCommand.class);
        verify(persistPriceTickUseCase).handle(captor.capture());
        assertThat(captor.getValue().symbol()).isEqualTo("BTCUSDT");
        assertThat(captor.getValue().price()).isEqualByComparingTo("65000.00");
        assertThat(captor.getValue().timestamp()).isEqualTo(ts);
    }

    @Test
    void handleDlt_logsWithoutThrowing_andNeverTouchesUseCase() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), Instant.now());

        assertThatCode(() -> consumer.handleDlt(tick, 42L, "boom")).doesNotThrowAnyException();
        verifyNoInteractions(persistPriceTickUseCase);
    }

    @Test
    void handleDlt_incrementsDltCounter() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), Instant.now());

        consumer.handleDlt(tick, 42L, "boom");

        assertThat(meterRegistry.counter("marketdata.kafka.dlt", "consumer", "tick-persister").count())
                .isEqualTo(1.0);
    }
}
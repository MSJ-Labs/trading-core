package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.PersistPriceTickCommand;
import com.msj.marketdata.application.command.PersistPriceTickUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceTickConsumerTest {

    @Mock
    private PersistPriceTickUseCase persistPriceTickUseCase;

    @InjectMocks
    private PriceTickConsumer consumer;

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
}
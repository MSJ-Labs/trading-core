package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceBroadcaster;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import com.msj.marketdata.infrastructure.ports.PriceTickPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceTickCommandHandlerTest {

    @Mock private PriceCache priceCache;
    @Mock private PriceBroadcaster priceBroadcaster;
    @Mock private PriceTickPublisher priceTickPublisher;

    @InjectMocks
    private PriceTickCommandHandler handler;

    @Test
    void handle_delegates_to_cache_broadcaster_and_publisher() {
        handler.handle(new PriceTickCommand("BTCUSDT", BigDecimal.valueOf(50_000), Instant.now()));

        verify(priceCache).updatePrice(any(PriceUpdate.class));
        verify(priceBroadcaster).broadcast(any(PriceUpdate.class));
        verify(priceTickPublisher).publish(any(PriceUpdate.class));
    }

    @Test
    void handle_builds_price_update_from_command_fields() {
        Instant ts = Instant.parse("2026-06-07T10:00:00Z");
        handler.handle(new PriceTickCommand("ETHUSDT", BigDecimal.valueOf(3_500), ts));

        ArgumentCaptor<PriceUpdate> captor = ArgumentCaptor.forClass(PriceUpdate.class);
        verify(priceBroadcaster).broadcast(captor.capture());
        PriceUpdate tick = captor.getValue();
        assertThat(tick.symbol()).isEqualTo("ETHUSDT");
        assertThat(tick.price()).isEqualByComparingTo("3500");
        assertThat(tick.timestamp()).isEqualTo(ts);
    }
}
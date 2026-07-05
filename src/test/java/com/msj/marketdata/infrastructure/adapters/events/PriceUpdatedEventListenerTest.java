package com.msj.marketdata.infrastructure.adapters.events;

import com.msj.marketdata.domain.events.PriceUpdatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

class PriceUpdatedEventListenerTest {

    @Test
    void onPriceUpdated_handlesEventWithoutThrowing() {
        PriceUpdatedEventListener listener = new PriceUpdatedEventListener();
        PriceUpdatedEvent event = new PriceUpdatedEvent("bitcoin", "BTC", BigDecimal.valueOf(60_000), Instant.now());

        assertThatCode(() -> listener.onPriceUpdated(event)).doesNotThrowAnyException();
    }
}
package com.msj.marketdata.domain.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdatedEvent(
        String coinId,
        String symbol,
        BigDecimal price,
        Instant occurredOn
) implements MarketDataEvent {}
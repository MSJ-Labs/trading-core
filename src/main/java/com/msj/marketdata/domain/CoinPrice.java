package com.msj.marketdata.domain;

import com.msj.marketdata.domain.events.PriceUpdatedEvent;
import com.msj.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Accessors(fluent = true)
@SuperBuilder
public class CoinPrice extends AggregateRoot<String> {

    private final String symbol;
    private final String name;
    private final BigDecimal priceUsd;
    private final BigDecimal priceChangePercent24h;
    private final BigDecimal marketCapUsd;
    private final BigDecimal volume24h;
    private final Instant lastUpdated;
    private final String imageUrl;

    public CoinPrice withUpdatedPrice(PriceUpdate tick) {
        CoinPrice updated = CoinPrice.builder()
                .id(coinId())
                .symbol(symbol)
                .name(name)
                .priceUsd(tick.price())
                .priceChangePercent24h(priceChangePercent24h)
                .marketCapUsd(marketCapUsd)
                .volume24h(volume24h)
                .lastUpdated(tick.timestamp())
                .imageUrl(imageUrl)
                .build();
        updated.registerEvent(new PriceUpdatedEvent(coinId(), symbol, tick.price(), tick.timestamp()));
        return updated;
    }

    public String coinId() { return id(); }
}
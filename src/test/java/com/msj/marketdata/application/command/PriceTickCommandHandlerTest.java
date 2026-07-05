package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.domain.events.PriceUpdatedEvent;
import com.msj.marketdata.infrastructure.ports.PriceBroadcaster;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import com.msj.marketdata.infrastructure.ports.PriceTickPublisher;
import com.msj.shared.domain.DomainEvent;
import com.msj.shared.domain.events.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceTickCommandHandlerTest {

    @Mock private PriceCache priceCache;
    @Mock private PriceBroadcaster priceBroadcaster;
    @Mock private PriceTickPublisher priceTickPublisher;
    @Mock private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PriceTickCommandHandler handler;

    private CoinPrice bitcoin() {
        return CoinPrice.builder()
                .id("bitcoin").symbol("BTC").name("Bitcoin")
                .priceUsd(BigDecimal.valueOf(50_000)).priceChangePercent24h(BigDecimal.ONE)
                .marketCapUsd(BigDecimal.ONE).volume24h(BigDecimal.ONE)
                .lastUpdated(Instant.now()).imageUrl(null)
                .build();
    }

    @Test
    void handle_broadcasts_and_publishes_raw_tick_unconditionally() {
        when(priceCache.findBySymbol("BTCUSDT")).thenReturn(List.of());

        handler.handle(new PriceTickCommand("BTCUSDT", BigDecimal.valueOf(50_000), Instant.now()));

        verify(priceBroadcaster).broadcast(any(PriceUpdate.class));
        verify(priceTickPublisher).publish(any(PriceUpdate.class));
    }

    @Test
    void handle_builds_price_update_from_command_fields() {
        Instant ts = Instant.parse("2026-06-07T10:00:00Z");
        when(priceCache.findBySymbol("ETHUSDT")).thenReturn(List.of());

        handler.handle(new PriceTickCommand("ETHUSDT", BigDecimal.valueOf(3_500), ts));

        ArgumentCaptor<PriceUpdate> captor = ArgumentCaptor.forClass(PriceUpdate.class);
        verify(priceBroadcaster).broadcast(captor.capture());
        PriceUpdate tick = captor.getValue();
        assertThat(tick.symbol()).isEqualTo("ETHUSDT");
        assertThat(tick.price()).isEqualByComparingTo("3500");
        assertThat(tick.timestamp()).isEqualTo(ts);
    }

    @Test
    void handle_withNoMatchingCachedCoin_doesNotTouchDomainEvents() {
        when(priceCache.findBySymbol("BTCUSDT")).thenReturn(List.of());

        handler.handle(new PriceTickCommand("BTCUSDT", BigDecimal.valueOf(50_000), Instant.now()));

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void handle_withMatchingCachedCoin_updatesCacheAndPublishesDomainEvent() {
        Instant ts = Instant.parse("2026-06-07T10:00:00Z");
        when(priceCache.findBySymbol("BTCUSDT")).thenReturn(List.of(bitcoin()));

        handler.handle(new PriceTickCommand("BTCUSDT", BigDecimal.valueOf(60_000), ts));

        ArgumentCaptor<CoinPrice> coinCaptor = ArgumentCaptor.forClass(CoinPrice.class);
        verify(priceCache).putCoinPrice(coinCaptor.capture());
        assertThat(coinCaptor.getValue().priceUsd()).isEqualByComparingTo("60000");

        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventPublisher).publish(eventsCaptor.capture());
        assertThat(eventsCaptor.getValue())
                .containsExactly(new PriceUpdatedEvent("bitcoin", "BTC", BigDecimal.valueOf(60_000), ts));
    }
}
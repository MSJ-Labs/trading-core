package com.msj.marketdata.application.query;

import com.msj.marketdata.domain.CoinNotFoundException;
import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.infrastructure.ports.MarketDataProvider;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCoinPriceQueryHandlerTest {

    @Mock
    private PriceCache priceCache;

    @Mock
    private MarketDataProvider marketDataProvider;

    @InjectMocks
    private GetCoinPriceQueryHandler handler;

    @Test
    void handle_returns_cached_price_without_hitting_provider() {
        CoinPrice cached = fakeCoin("xrp");
        when(priceCache.getCoinPrice("xrp")).thenReturn(Optional.of(cached));

        CoinPrice result = handler.handle(new GetCoinPriceQuery("xrp"));

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(marketDataProvider);
    }

    @Test
    void handle_fetches_from_provider_on_cache_miss_and_stores_result() {
        CoinPrice coin = fakeCoin("bitcoin");
        when(priceCache.getCoinPrice("bitcoin")).thenReturn(Optional.empty());
        when(marketDataProvider.fetchCoinPrice("bitcoin")).thenReturn(Optional.of(coin));

        CoinPrice result = handler.handle(new GetCoinPriceQuery("bitcoin"));

        assertThat(result).isEqualTo(coin);
        verify(priceCache).putCoinPrice(coin);
    }

    @Test
    void handle_throws_CoinNotFoundException_when_provider_returns_empty() {
        when(priceCache.getCoinPrice("unknown-coin")).thenReturn(Optional.empty());
        when(marketDataProvider.fetchCoinPrice("unknown-coin")).thenReturn(Optional.empty());
        GetCoinPriceQuery query = new GetCoinPriceQuery("unknown-coin");

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("unknown-coin");
    }

    @Test
    void handle_does_not_cache_when_provider_throws() {
        when(priceCache.getCoinPrice("bitcoin")).thenReturn(Optional.empty());
        when(marketDataProvider.fetchCoinPrice("bitcoin")).thenThrow(new RuntimeException("timeout"));
        GetCoinPriceQuery query = new GetCoinPriceQuery("bitcoin");

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(RuntimeException.class);
        verify(priceCache, never()).putCoinPrice(any());
    }

    private CoinPrice fakeCoin(String coinId) {
        return CoinPrice.builder()
                .id(coinId).symbol(coinId.toUpperCase()).name(coinId)
                .priceUsd(BigDecimal.valueOf(50000)).priceChangePercent24h(BigDecimal.valueOf(2.5))
                .marketCapUsd(BigDecimal.valueOf(1_000_000_000)).volume24h(BigDecimal.valueOf(30_000_000))
                .lastUpdated(Instant.now()).imageUrl(null)
                .build();
    }
}

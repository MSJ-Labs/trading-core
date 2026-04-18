package com.msj.marketdata.application.query;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTopCoinsQueryHandlerTest {

    @Mock
    private PriceCache priceCache;

    @Mock
    private MarketDataProvider marketDataProvider;

    @InjectMocks
    private GetTopCoinsQueryHandler handler;

    @Test
    void handle_returns_cached_coins_without_hitting_provider() {
        List<CoinPrice> cached = List.of(fakeCoin("bitcoin"), fakeCoin("ethereum"));
        when(priceCache.getTopCoins()).thenReturn(cached);

        List<CoinPrice> result = handler.handle(new GetTopCoinsQuery(2));

        assertThat(result).hasSize(2);
        verifyNoInteractions(marketDataProvider);
    }

    @Test
    void handle_fetches_from_provider_on_cache_miss_and_stores_result() {
        List<CoinPrice> coins = List.of(fakeCoin("bitcoin"), fakeCoin("ethereum"));
        when(priceCache.getTopCoins()).thenReturn(List.of());
        when(marketDataProvider.fetchTopCoins(2)).thenReturn(coins);

        List<CoinPrice> result = handler.handle(new GetTopCoinsQuery(2));

        assertThat(result).hasSize(2);
        verify(priceCache).putTopCoins(coins);
    }

    @Test
    void handle_limits_cached_result_to_requested_count() {
        List<CoinPrice> cached = List.of(fakeCoin("bitcoin"), fakeCoin("ethereum"), fakeCoin("solana"));
        when(priceCache.getTopCoins()).thenReturn(cached);

        List<CoinPrice> result = handler.handle(new GetTopCoinsQuery(2));

        assertThat(result).hasSize(2);
    }

    @Test
    void handle_returns_all_cached_coins_when_limit_exceeds_cache_size() {
        List<CoinPrice> cached = List.of(fakeCoin("bitcoin"));
        when(priceCache.getTopCoins()).thenReturn(cached);

        List<CoinPrice> result = handler.handle(new GetTopCoinsQuery(5));

        assertThat(result).hasSize(1);
    }

    private CoinPrice fakeCoin(String coinId) {
        return new CoinPrice(coinId, coinId.toUpperCase(), coinId,
                BigDecimal.valueOf(50000), BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(1_000_000_000), BigDecimal.valueOf(30_000_000), Instant.now());
    }
}

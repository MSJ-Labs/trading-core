package com.msj.marketdata.infrastructure.adapters.cache;

import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPriceCacheTest {

    private InMemoryPriceCache cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryPriceCache();
    }

    @Test
    void getTopCoins_returns_empty_list_when_nothing_cached() {
        assertThat(cache.getTopCoins()).isEmpty();
    }

    @Test
    void putTopCoins_then_getTopCoins_returns_stored_list() {
        List<CoinPrice> coins = List.of(fakeCoin("bitcoin", "BTC"), fakeCoin("ethereum", "ETH"));
        cache.putTopCoins(coins);

        assertThat(cache.getTopCoins()).hasSize(2);
    }

    @Test
    void putTopCoins_also_populates_individual_coin_entries() {
        cache.putTopCoins(List.of(fakeCoin("bitcoin", "BTC")));

        assertThat(cache.getCoinPrice("bitcoin")).isPresent()
                .hasValueSatisfying(c -> assertThat(c.coinId()).isEqualTo("bitcoin"));
    }

    @Test
    void getCoinPrice_returns_empty_when_not_cached() {
        assertThat(cache.getCoinPrice("solana")).isEmpty();
    }

    @Test
    void putCoinPrice_then_getCoinPrice_returns_stored_entry() {
        cache.putCoinPrice(fakeCoin("solana", "SOL"));

        Optional<CoinPrice> result = cache.getCoinPrice("solana");
        assertThat(result).isPresent();
        assertThat(result.get().symbol()).isEqualTo("SOL");
    }

    @Test
    void updatePrice_updates_cached_entry_matching_symbol_exactly() {
        cache.putCoinPrice(fakeCoin("bitcoin", "BTC"));

        cache.updatePrice("BTC", BigDecimal.valueOf(100_000), Instant.now());

        assertThat(cache.getCoinPrice("bitcoin"))
                .isPresent()
                .hasValueSatisfying(c -> assertThat(c.priceUsd()).isEqualByComparingTo("100000"));
    }

    @Test
    void updatePrice_matches_symbol_plus_usdt_suffix() {
        cache.putCoinPrice(fakeCoin("ethereum", "ETH"));

        cache.updatePrice("ETHUSDT", BigDecimal.valueOf(3_500), Instant.now());

        assertThat(cache.getCoinPrice("ethereum"))
                .isPresent()
                .hasValueSatisfying(c -> assertThat(c.priceUsd()).isEqualByComparingTo("3500"));
    }

    @Test
    void updatePrice_is_case_insensitive() {
        cache.putCoinPrice(fakeCoin("solana", "SOL"));

        cache.updatePrice("sol", BigDecimal.valueOf(200), Instant.now());

        assertThat(cache.getCoinPrice("solana"))
                .isPresent()
                .hasValueSatisfying(c -> assertThat(c.priceUsd()).isEqualByComparingTo("200"));
    }

    @Test
    void updatePrice_does_nothing_when_no_matching_symbol() {
        cache.putCoinPrice(fakeCoin("bitcoin", "BTC"));

        cache.updatePrice("ETHUSDT", BigDecimal.valueOf(3_000), Instant.now());

        assertThat(cache.getCoinPrice("bitcoin"))
                .isPresent()
                .hasValueSatisfying(c -> assertThat(c.priceUsd()).isEqualByComparingTo("50000"));
    }

    private CoinPrice fakeCoin(String coinId, String symbol) {
        return new CoinPrice(coinId, symbol, coinId,
                BigDecimal.valueOf(50_000), BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(1_000_000_000), BigDecimal.valueOf(20_000_000), Instant.now(), null);
    }
}

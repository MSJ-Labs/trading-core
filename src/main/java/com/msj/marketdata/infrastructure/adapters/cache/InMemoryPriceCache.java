package com.msj.marketdata.infrastructure.adapters.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryPriceCache implements PriceCache {

    private static final String TOP_COINS_KEY = "topCoins";

    private final Cache<String, List<CoinPrice>> topCoinsCache = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(1)
            .build();

    private final Cache<String, CoinPrice> coinCache = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(250)
            .build();

    @Override
    public List<CoinPrice> getTopCoins() {
        List<CoinPrice> result = topCoinsCache.getIfPresent(TOP_COINS_KEY);
        return result != null ? result : List.of();
    }

    @Override
    public void putTopCoins(List<CoinPrice> coins) {
        topCoinsCache.put(TOP_COINS_KEY, coins);
        coins.forEach(c -> coinCache.put(c.coinId(), c));
    }

    @Override
    public Optional<CoinPrice> getCoinPrice(String coinId) {
        return Optional.ofNullable(coinCache.getIfPresent(coinId));
    }

    @Override
    public void putCoinPrice(CoinPrice coinPrice) {
        coinCache.put(coinPrice.coinId(), coinPrice);
    }

    @Override
    public void updatePrice(PriceUpdate tick) {
        coinCache.asMap().forEach((coinId, coinPrice) -> {
            boolean matchesSymbol = coinPrice.symbol().equalsIgnoreCase(tick.symbol());
            boolean matchesSymbolWithUsdt = (coinPrice.symbol() + "USDT").equalsIgnoreCase(tick.symbol());
            if (matchesSymbol || matchesSymbolWithUsdt) {
                coinCache.put(coinId, coinPrice.withUpdatedPrice(tick));
            }
        });
    }
}

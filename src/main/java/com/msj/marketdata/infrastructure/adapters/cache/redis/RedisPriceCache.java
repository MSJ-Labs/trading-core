package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Redis-backed adapter for {@link PriceCache} — the default price cache, active whenever
 * multiple app instances share one cache instead of each holding its own in-process copy
 * (see the Caffeine adapter, kept as a single-instance alternative).
 */
@Component
@ConditionalOnProperty(name = "marketdata.cache.provider", havingValue = "redis", matchIfMissing = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RedisPriceCache implements PriceCache {

    private static final String TOP_COINS_KEY = "price:topCoins";
    private static final String COIN_KEY_PREFIX = "price:coin:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, CoinPriceRecord> coinTemplate;
    private final RedisTemplate<String, List<CoinPriceRecord>> topCoinsTemplate;

    @Override
    public List<CoinPrice> getTopCoins() {
        List<CoinPriceRecord> records = topCoinsTemplate.opsForValue().get(TOP_COINS_KEY);
        return records == null ? List.of() : records.stream().map(CoinPriceRecord::toDomain).toList();
    }

    @Override
    public void putTopCoins(List<CoinPrice> coins) {
        List<CoinPriceRecord> records = coins.stream().map(CoinPriceRecord::fromDomain).toList();
        topCoinsTemplate.opsForValue().set(TOP_COINS_KEY, records, TTL);
        coins.forEach(this::putCoinPrice);
    }

    @Override
    public Optional<CoinPrice> getCoinPrice(String coinId) {
        return Optional.ofNullable(coinTemplate.opsForValue().get(COIN_KEY_PREFIX + coinId))
                .map(CoinPriceRecord::toDomain);
    }

    @Override
    public void putCoinPrice(CoinPrice coinPrice) {
        coinTemplate.opsForValue().set(COIN_KEY_PREFIX + coinPrice.coinId(), CoinPriceRecord.fromDomain(coinPrice), TTL);
    }

    @Override
    public List<CoinPrice> findBySymbol(String symbol) {
        Set<String> keys = scanCoinKeys();
        if (keys.isEmpty()) {
            return List.of();
        }
        return keys.stream()
                .map(coinTemplate.opsForValue()::get)
                .filter(Objects::nonNull)
                .filter(record -> matches(record, symbol))
                .map(CoinPriceRecord::toDomain)
                .toList();
    }

    // SCAN (cursor-based, non-blocking) instead of KEYS — safe to call from a request thread
    // even as the keyspace grows, unlike KEYS which blocks the single-threaded Redis event loop.
    private Set<String> scanCoinKeys() {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(COIN_KEY_PREFIX + "*").count(100).build();
        try (Cursor<String> cursor = coinTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        return keys;
    }

    private static boolean matches(CoinPriceRecord record, String symbol) {
        boolean matchesSymbol = record.symbol().equalsIgnoreCase(symbol);
        boolean matchesSymbolWithUsdt = (record.symbol() + "USDT").equalsIgnoreCase(symbol);
        return matchesSymbol || matchesSymbolWithUsdt;
    }
}
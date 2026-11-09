package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.application.port.out.PriceCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Redis-backed adapter for {@link PriceCache} — the default price cache, active whenever
 * multiple app instances share one cache instead of each holding its own in-process copy
 * (see the Caffeine adapter, kept as a single-instance alternative).
 *
 * <p>Two secondary indexes avoid ever scanning the coin keyspace: a Sorted Set preserving
 * top-coins rank order, and a Set per symbol for direct {@link #findBySymbol} lookup. Both
 * are rebuilt/appended on write and carry no TTL of their own — a coin dropping out of the
 * index is harmless because {@link #getCoinPrice} already treats a missing/expired coin key
 * as absent.
 */
@Component
@ConditionalOnProperty(name = "marketdata.cache.provider", havingValue = "redis", matchIfMissing = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RedisPriceCache implements PriceCache {

    private static final String COIN_KEY_PREFIX = "price:coin:";
    private static final String IDENTITY_KEY_PREFIX = "price:identity:";
    private static final String TOP_INDEX_KEY = "price:index:top";
    private static final String TOP_INDEX_TMP_KEY = "price:index:top:tmp";
    private static final String SYMBOL_INDEX_PREFIX = "price:index:symbol:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, CoinPriceRecord> coinTemplate;
    private final RedisTemplate<String, CoinIdentityRecord> identityTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CoinPrice> getTopCoins() {
        // LinkedHashSet from Redis keeps rank order; fetchCoins preserves it.
        Set<String> coinIds = stringRedisTemplate.opsForZSet().range(TOP_INDEX_KEY, 0, -1);
        return fetchCoins(coinIds);
    }

    @Override
    public void putTopCoins(List<CoinPrice> coins) {
        if (coins.isEmpty()) {
            return;
        }
        coins.forEach(this::putCoinPrice);
        identityTemplate.opsForValue().multiSet(coins.stream().collect(Collectors.toMap(
                coin -> IDENTITY_KEY_PREFIX + coin.coinId(), CoinIdentityRecord::fromDomain, (a, b) -> b)));
        // Build the new ranking aside then RENAME: atomic swap, readers never see an empty index.
        Set<ZSetOperations.TypedTuple<String>> ranked = IntStream.range(0, coins.size())
                .mapToObj(rank -> ZSetOperations.TypedTuple.of(coins.get(rank).coinId(), (double) rank))
                .collect(Collectors.toSet());
        stringRedisTemplate.delete(TOP_INDEX_TMP_KEY);
        stringRedisTemplate.opsForZSet().add(TOP_INDEX_TMP_KEY, ranked);
        stringRedisTemplate.rename(TOP_INDEX_TMP_KEY, TOP_INDEX_KEY);
    }

    @Override
    public Optional<CoinPrice> getCoinPrice(String coinId) {
        return Optional.ofNullable(coinTemplate.opsForValue().get(COIN_KEY_PREFIX + coinId))
                .map(CoinPriceRecord::toDomain);
    }

    @Override
    public void putCoinPrice(CoinPrice coinPrice) {
        coinTemplate.opsForValue().set(COIN_KEY_PREFIX + coinPrice.coinId(), CoinPriceRecord.fromDomain(coinPrice), TTL);
        stringRedisTemplate.opsForSet().add(symbolIndexKey(coinPrice.symbol()), coinPrice.coinId());
    }

    @Override
    public List<CoinPrice> findBySymbol(String symbol) {
        Set<String> coinIds = stringRedisTemplate.opsForSet().members(symbolIndexKey(canonicalSymbol(symbol)));
        return fetchCoins(coinIds);
    }

    private List<CoinPrice> fetchCoins(Set<String> coinIds) {
        if (coinIds == null || coinIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = List.copyOf(coinIds);
        List<CoinPriceRecord> prices = coinTemplate.opsForValue()
                .multiGet(ids.stream().map(id -> COIN_KEY_PREFIX + id).toList());
        List<CoinIdentityRecord> identities = identityTemplate.opsForValue()
                .multiGet(ids.stream().map(id -> IDENTITY_KEY_PREFIX + id).toList());
        List<CoinPrice> result = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            CoinPriceRecord price = prices == null ? null : prices.get(i);
            CoinIdentityRecord identity = identities == null ? null : identities.get(i);
            if (price != null) {
                result.add(price.toDomain());
            } else if (identity != null) {
                // Price snapshot expired: keep the coin, without price.
                result.add(identity.toDomainWithoutPrice());
            }
        }
        return result;
    }

    private static String symbolIndexKey(String symbol) {
        return SYMBOL_INDEX_PREFIX + symbol.toUpperCase(Locale.ROOT);
    }

    // Binance-style tickers append "USDT" to the base symbol (e.g. "BTCUSDT"); the index is
    // keyed by the base symbol only, so a query strips that suffix before looking it up.
    private static String canonicalSymbol(String symbol) {
        String upper = symbol.toUpperCase(Locale.ROOT);
        return upper.length() > 4 && upper.endsWith("USDT") ? upper.substring(0, upper.length() - 4) : upper;
    }
}
package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Template-level unit test: covers the merge of price snapshot and identity (the expired-price
 * bug). Real Redis behaviour (TTL, RENAME) is not covered here.
 */
@ExtendWith(MockitoExtension.class)
class RedisPriceCacheTest {

    @Mock
    private RedisTemplate<String, CoinPriceRecord> coinTemplate;
    @Mock
    private RedisTemplate<String, CoinIdentityRecord> identityTemplate;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, CoinPriceRecord> priceOps;
    @Mock
    private ValueOperations<String, CoinIdentityRecord> identityOps;
    @Mock
    private ZSetOperations<String, String> zSetOps;
    @Mock
    private SetOperations<String, String> setOps;

    private RedisPriceCache cache;

    @BeforeEach
    void setUp() {
        cache = new RedisPriceCache(coinTemplate, identityTemplate, stringRedisTemplate);
    }

    @Test
    void getTopCoins_keeps_coin_without_price_when_its_snapshot_expired() {
        stubRanking("bitcoin", "ethereum");
        when(coinTemplate.opsForValue()).thenReturn(priceOps);
        when(identityTemplate.opsForValue()).thenReturn(identityOps);
        when(priceOps.multiGet(List.of("price:coin:bitcoin", "price:coin:ethereum")))
                .thenReturn(Arrays.asList(priceRecord("bitcoin"), null));
        when(identityOps.multiGet(List.of("price:identity:bitcoin", "price:identity:ethereum")))
                .thenReturn(List.of(identityRecord("bitcoin"), identityRecord("ethereum")));

        List<CoinPrice> result = cache.getTopCoins();

        assertThat(result).extracting(CoinPrice::coinId).containsExactly("bitcoin", "ethereum");
        assertThat(result.get(0).priceUsd()).isEqualByComparingTo("50000");
        assertThat(result.get(1).priceUsd()).isNull();
        assertThat(result.get(1).name()).isEqualTo("ethereum-name");
    }

    @Test
    void getTopCoins_skips_coin_with_neither_price_nor_identity() {
        stubRanking("bitcoin", "ghost");
        when(coinTemplate.opsForValue()).thenReturn(priceOps);
        when(identityTemplate.opsForValue()).thenReturn(identityOps);
        when(priceOps.multiGet(List.of("price:coin:bitcoin", "price:coin:ghost")))
                .thenReturn(Arrays.asList(priceRecord("bitcoin"), null));
        when(identityOps.multiGet(List.of("price:identity:bitcoin", "price:identity:ghost")))
                .thenReturn(Arrays.asList(identityRecord("bitcoin"), null));

        assertThat(cache.getTopCoins()).extracting(CoinPrice::coinId).containsExactly("bitcoin");
    }

    @Test
    void getTopCoins_returns_empty_when_index_is_empty() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("price:index:top", 0, -1)).thenReturn(Set.of());

        assertThat(cache.getTopCoins()).isEmpty();
        verifyNoInteractions(coinTemplate, identityTemplate);
    }

    @Test
    void putTopCoins_ignores_empty_list_to_keep_existing_cache() {
        cache.putTopCoins(List.of());

        verifyNoInteractions(coinTemplate, identityTemplate, stringRedisTemplate);
    }

    @Test
    void putTopCoins_stores_identities_then_swaps_ranking_atomically() {
        when(coinTemplate.opsForValue()).thenReturn(priceOps);
        when(identityTemplate.opsForValue()).thenReturn(identityOps);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOps);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);

        cache.putTopCoins(List.of(domainCoin("bitcoin"), domainCoin("ethereum")));

        verify(identityOps).multiSet(anyMap());
        var order = inOrder(zSetOps, stringRedisTemplate);
        order.verify(zSetOps).add(eq("price:index:top:tmp"), any(Set.class));
        order.verify(stringRedisTemplate).rename("price:index:top:tmp", "price:index:top");
    }

    private void stubRanking(String... coinIds) {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range("price:index:top", 0, -1)).thenReturn(new LinkedHashSet<>(List.of(coinIds)));
    }

    private static CoinPriceRecord priceRecord(String coinId) {
        return new CoinPriceRecord(coinId, coinId.toUpperCase(), coinId + "-name",
                BigDecimal.valueOf(50_000), BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TWO,
                Instant.parse("2026-11-09T00:00:00Z"), null);
    }

    private static CoinIdentityRecord identityRecord(String coinId) {
        return new CoinIdentityRecord(coinId, coinId.toUpperCase(), coinId + "-name", null);
    }

    private static CoinPrice domainCoin(String coinId) {
        return priceRecord(coinId).toDomain();
    }
}
package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** Real Redis (Docker): TTL on prices, no TTL on identities, atomic ranking swap. */
@Testcontainers
class RedisPriceCacheContainerTest {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Testcontainers
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate strings;
    private static RedisPriceCache cache;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(REDIS.getHost(), REDIS.getMappedPort(6379)));
        connectionFactory.afterPropertiesSet();
        strings = new StringRedisTemplate(connectionFactory);

        JsonMapper jsonMapper = JsonMapper.builder().build();
        RedisCacheConfig config = new RedisCacheConfig();
        cache = new RedisPriceCache(
                config.coinPriceRedisTemplate(connectionFactory, jsonMapper),
                config.coinIdentityRedisTemplate(connectionFactory, jsonMapper),
                strings);
    }

    @AfterAll
    static void disconnect() {
        connectionFactory.destroy();
    }

    @BeforeEach
    void flush() {
        strings.execute((org.springframework.data.redis.core.RedisCallback<Object>) c -> {
            c.serverCommands().flushAll();
            return null;
        });
    }

    @Test
    void price_key_has_a_ttl_but_identity_key_has_none() {
        cache.putTopCoins(List.of(coin("bitcoin", 0)));

        assertThat(strings.getExpire("price:coin:bitcoin")).isPositive().isLessThanOrEqualTo(60);
        assertThat(strings.getExpire("price:identity:bitcoin")).isEqualTo(-1L);
    }

    @Test
    void expired_price_keeps_the_coin_in_top_list_without_price() {
        cache.putTopCoins(List.of(coin("bitcoin", 0), coin("ethereum", 1)));

        strings.delete("price:coin:ethereum"); // what the 60 s TTL does

        List<CoinPrice> top = cache.getTopCoins();
        assertThat(top).extracting(CoinPrice::coinId).containsExactly("bitcoin", "ethereum");
        assertThat(top.get(0).priceUsd()).isEqualByComparingTo("1000");
        assertThat(top.get(1).priceUsd()).isNull();
        assertThat(top.get(1).name()).isEqualTo("ethereum-name");
    }

    @Test
    void all_250_coins_are_returned_in_rank_order_when_most_prices_expired() {
        List<CoinPrice> coins = IntStream.range(0, 250).mapToObj(i -> coin("coin-" + i, i)).toList();
        cache.putTopCoins(coins);
        IntStream.range(8, 250).forEach(i -> strings.delete("price:coin:coin-" + i));

        List<CoinPrice> top = cache.getTopCoins();

        assertThat(top).hasSize(250);
        assertThat(top).extracting(CoinPrice::coinId)
                .containsExactlyElementsOf(coins.stream().map(CoinPrice::coinId).toList());
        assertThat(top.stream().filter(c -> c.priceUsd() != null)).hasSize(8);
    }

    @Test
    void putTopCoins_replaces_the_ranking_and_leaves_no_temporary_key() {
        cache.putTopCoins(List.of(coin("bitcoin", 0), coin("ethereum", 1)));
        cache.putTopCoins(List.of(coin("solana", 0), coin("bitcoin", 1)));

        assertThat(cache.getTopCoins()).extracting(CoinPrice::coinId).containsExactly("solana", "bitcoin");
        assertThat(strings.hasKey("price:index:top:tmp")).isFalse();
    }

    @Test
    void findBySymbol_returns_coin_without_price_so_a_tick_can_revive_it() {
        cache.putTopCoins(List.of(coin("bitcoin", 0)));
        strings.delete("price:coin:bitcoin");

        List<CoinPrice> found = cache.findBySymbol("BTCUSDT");

        assertThat(found).extracting(CoinPrice::coinId).containsExactly("bitcoin");
        assertThat(found.get(0).priceUsd()).isNull();
    }

    private static CoinPrice coin(String coinId, int rank) {
        return CoinPrice.builder()
                .id(coinId)
                .symbol(coinId.equals("bitcoin") ? "BTC" : coinId.toUpperCase())
                .name(coinId + "-name")
                .priceUsd(BigDecimal.valueOf(1000 + rank))
                .priceChangePercent24h(BigDecimal.ONE)
                .marketCapUsd(BigDecimal.TEN)
                .volume24h(BigDecimal.TWO)
                .lastUpdated(Instant.parse("2026-11-09T00:00:00Z"))
                .build();
    }
}
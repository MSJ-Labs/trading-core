package com.msj.marketdata.infrastructure.adapters.cache.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@ConditionalOnProperty(name = "marketdata.cache.provider", havingValue = "redis", matchIfMissing = true)
class RedisCacheConfig {

    // StringRedisTemplate (used for the top-coins Sorted Set and per-symbol Set indexes) is
    // already auto-configured by Spring Boot whenever a RedisConnectionFactory bean exists —
    // only the CoinPriceRecord value template needs a bean defined here.
    @Bean
    RedisTemplate<String, CoinPriceRecord> coinPriceRedisTemplate(RedisConnectionFactory connectionFactory,
                                                                  JsonMapper jsonMapper) {
        return jsonTemplate(connectionFactory, new JacksonJsonRedisSerializer<>(jsonMapper, CoinPriceRecord.class));
    }

    @Bean
    RedisTemplate<String, CoinIdentityRecord> coinIdentityRedisTemplate(RedisConnectionFactory connectionFactory,
                                                                        JsonMapper jsonMapper) {
        return jsonTemplate(connectionFactory, new JacksonJsonRedisSerializer<>(jsonMapper, CoinIdentityRecord.class));
    }

    private static <T> RedisTemplate<String, T> jsonTemplate(RedisConnectionFactory connectionFactory,
                                                             JacksonJsonRedisSerializer<T> serializer) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
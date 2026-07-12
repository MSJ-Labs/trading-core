package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "marketdata.cache.provider", havingValue = "redis", matchIfMissing = true)
class RedisCacheConfig {

    // StringRedisTemplate (used for the top-coins Sorted Set and per-symbol Set indexes) is
    // already auto-configured by Spring Boot whenever a RedisConnectionFactory bean exists —
    // only the CoinPriceRecord value template needs a bean defined here.
    @Bean
    RedisTemplate<String, CoinPriceRecord> coinPriceRedisTemplate(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<CoinPriceRecord> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper(), CoinPriceRecord.class);
        RedisTemplate<String, CoinPriceRecord> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    // A manually constructed ObjectMapper doesn't get Spring Boot's Jackson auto-configuration
    // (that's what wires JavaTimeModule in everywhere else — REST responses, Kafka JSON
    // (de)serializers), so it has to be registered explicitly here.
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
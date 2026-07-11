package com.msj.marketdata.infrastructure.adapters.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "marketdata.cache.provider", havingValue = "redis", matchIfMissing = true)
class RedisCacheConfig {

    @Bean
    RedisTemplate<String, CoinPriceRecord> coinPriceRedisTemplate(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<CoinPriceRecord> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper(), CoinPriceRecord.class);
        return buildTemplate(connectionFactory, serializer);
    }

    @Bean
    RedisTemplate<String, List<CoinPriceRecord>> coinPriceListRedisTemplate(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = redisObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, CoinPriceRecord.class);
        Jackson2JsonRedisSerializer<List<CoinPriceRecord>> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, listType);
        return buildTemplate(connectionFactory, serializer);
    }

    private <V> RedisTemplate<String, V> buildTemplate(RedisConnectionFactory connectionFactory,
                                                         Jackson2JsonRedisSerializer<V> valueSerializer) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
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
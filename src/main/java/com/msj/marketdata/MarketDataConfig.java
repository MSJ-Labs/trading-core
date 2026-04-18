package com.msj.marketdata;

import com.msj.marketdata.infrastructure.adapters.binance.BinanceProperties;
import com.msj.marketdata.infrastructure.adapters.coingecko.CoinGeckoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({CoinGeckoProperties.class, BinanceProperties.class})
public class MarketDataConfig {

    @Bean
    public RestClient coinGeckoRestClient(CoinGeckoProperties properties) {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
            builder.defaultHeader("x-cg-demo-api-key", properties.apiKey());
        }
        return builder.build();
    }
}

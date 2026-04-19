package com.msj.marketdata.infrastructure.adapters.coingecko;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coingecko")
public record CoinGeckoProperties(
        String baseUrl,
        String apiKey,
        int topCoinsLimit
) {}
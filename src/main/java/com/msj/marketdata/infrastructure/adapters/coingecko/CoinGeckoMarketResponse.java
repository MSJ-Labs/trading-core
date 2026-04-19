package com.msj.marketdata.infrastructure.adapters.coingecko;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CoinGeckoMarketResponse(
        String id,
        String symbol,
        String name,
        @JsonProperty("current_price") BigDecimal currentPrice,
        @JsonProperty("price_change_percentage_24h") BigDecimal priceChangePercentage24h,
        @JsonProperty("market_cap") BigDecimal marketCap,
        @JsonProperty("total_volume") BigDecimal totalVolume,
        @JsonProperty("last_updated") String lastUpdated,
        String image
) {}

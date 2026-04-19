package com.msj.marketdata.infrastructure.adapters.binance;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BinanceMiniTickerMessage(
        @JsonProperty("e") String eventType,
        @JsonProperty("s") String symbol,
        @JsonProperty("c") BigDecimal closePrice,
        @JsonProperty("E") Long eventTime
) {}

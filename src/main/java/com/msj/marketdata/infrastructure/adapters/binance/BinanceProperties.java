package com.msj.marketdata.infrastructure.adapters.binance;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "binance")
public record BinanceProperties(
        String websocketUrl,
        List<String> defaultSymbols
) {}

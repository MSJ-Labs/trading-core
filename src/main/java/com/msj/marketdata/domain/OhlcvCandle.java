package com.msj.marketdata.domain;

import com.msj.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.time.Instant;

public record OhlcvCandle(
        String symbol,
        Instant openTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        int priceUpdateCount
) implements ValueObject {

    public static OhlcvCandle create(String symbol, Instant openTime, BigDecimal firstPrice) {
        return new OhlcvCandle(symbol, openTime, firstPrice, firstPrice, firstPrice, firstPrice, 1);
    }

    public OhlcvCandle withTick(BigDecimal price) {
        return new OhlcvCandle(
                symbol, openTime, open,
                price.compareTo(high) > 0 ? price : high,
                price.compareTo(low) < 0 ? price : low,
                price,
                priceUpdateCount + 1
        );
    }
}
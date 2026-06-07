package com.msj.marketdata.domain;

import com.msj.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdate(
        String symbol,
        BigDecimal price,
        Instant timestamp
) implements ValueObject {}
package com.msj.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdate(
        String symbol,
        BigDecimal price,
        Instant timestamp
) {}
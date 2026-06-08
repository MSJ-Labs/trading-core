package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "ohlcv_1m")
@CompoundIndex(name = "symbol_openTime_idx", def = "{'symbol': 1, 'openTime': -1}", unique = true)
public record OhlcvDocument(
        @Id String id,
        String symbol,
        Instant openTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        int priceUpdateCount
) {}
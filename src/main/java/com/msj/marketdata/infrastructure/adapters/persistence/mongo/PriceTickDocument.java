package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.PriceUpdate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "price_ticks")
@CompoundIndex(name = "symbol_timestamp_idx", def = "{'symbol': 1, 'timestamp': -1}")
public record PriceTickDocument(
        @Id String id,
        String symbol,
        BigDecimal price,
        Instant timestamp
) {
    static PriceTickDocument from(PriceUpdate tick) {
        String id = tick.symbol() + "_" + tick.timestamp().toEpochMilli();
        return new PriceTickDocument(id, tick.symbol(), tick.price(), tick.timestamp());
    }
}
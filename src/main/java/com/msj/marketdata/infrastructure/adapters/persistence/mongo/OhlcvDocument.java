package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.OhlcvCandle;
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
) {
    static OhlcvDocument from(OhlcvCandle candle) {
        String id = candle.symbol() + "_" + candle.openTime().toEpochMilli();
        return new OhlcvDocument(
                id,
                candle.symbol(),
                candle.openTime(),
                candle.open(),
                candle.high(),
                candle.low(),
                candle.close(),
                candle.priceUpdateCount()
        );
    }
}
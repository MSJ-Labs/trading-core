package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.adapters.persistence.mongo.OhlcvDocument;
import com.msj.marketdata.infrastructure.ports.OhlcvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OhlcvAggregator {

    private final OhlcvRepository ohlcvRepository;
    private final ConcurrentHashMap<String, OhlcvDocument> openCandles = new ConcurrentHashMap<>();

    public void onTick(PriceUpdate tick) {
        Instant bucketStart = minuteBucket(tick.timestamp());
        openCandles.compute(tick.symbol(), (symbol, current) -> {
            if (current == null || !current.openTime().equals(bucketStart)) {
                if (current != null) {
                    ohlcvRepository.save(current);
                }
                return OhlcvDocument.create(symbol, bucketStart, tick.price());
            }
            return current.withTick(tick.price());
        });
    }

    Optional<OhlcvDocument> currentCandle(String symbol) {
        return Optional.ofNullable(openCandles.get(symbol));
    }

    private static Instant minuteBucket(Instant ts) {
        long epochSeconds = ts.getEpochSecond();
        return Instant.ofEpochSecond(epochSeconds - (epochSeconds % 60));
    }
}
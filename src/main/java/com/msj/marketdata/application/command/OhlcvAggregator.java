package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.OhlcvCandle;
import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.OhlcvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OhlcvAggregator implements OhlcvAggregationUseCase {

    private final OhlcvRepository ohlcvRepository;
    private final ConcurrentHashMap<String, OhlcvCandle> openCandles = new ConcurrentHashMap<>();

    @Override
    public void onTick(PriceUpdate tick) {
        Instant bucketStart = minuteBucket(tick.timestamp());
        openCandles.compute(tick.symbol(), (symbol, current) -> {
            if (current == null || !current.openTime().equals(bucketStart)) {
                if (current != null) {
                    ohlcvRepository.save(current);
                }
                return OhlcvCandle.create(symbol, bucketStart, tick.price());
            }
            return current.withTick(tick.price());
        });
    }

    Optional<OhlcvCandle> currentCandle(String symbol) {
        return Optional.ofNullable(openCandles.get(symbol));
    }

    private static Instant minuteBucket(Instant ts) {
        long epochSeconds = ts.getEpochSecond();
        return Instant.ofEpochSecond(epochSeconds - (epochSeconds % 60));
    }
}
package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.OhlcvCandle;
import com.msj.marketdata.infrastructure.ports.OhlcvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDbOhlcvRepositoryAdapter implements OhlcvRepository {

    private final SpringDataOhlcvRepository repository;

    @Override
    public void save(OhlcvCandle candle) {
        repository.save(new OhlcvDocument(
                null,
                candle.symbol(),
                candle.openTime(),
                candle.open(),
                candle.high(),
                candle.low(),
                candle.close(),
                candle.priceUpdateCount()
        ));
    }
}
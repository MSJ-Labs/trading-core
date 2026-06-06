package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.infrastructure.ports.OhlcvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDbOhlcvRepositoryAdapter implements OhlcvRepository {

    private final SpringDataOhlcvRepository repository;

    @Override
    public void save(OhlcvDocument candle) {
        repository.save(candle);
    }
}
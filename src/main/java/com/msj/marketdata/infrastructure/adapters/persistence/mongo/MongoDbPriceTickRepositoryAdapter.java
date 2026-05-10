package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceTickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDbPriceTickRepositoryAdapter implements PriceTickRepository {

    private final SpringDataPriceTickRepository repository;

    @Override
    public void save(PriceUpdate tick) {
        repository.save(PriceTickDocument.from(tick));
    }
}
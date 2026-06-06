package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MongoDbOhlcvRepositoryAdapterTest {

    @Mock
    private SpringDataOhlcvRepository repository;

    @InjectMocks
    private MongoDbOhlcvRepositoryAdapter adapter;

    @Test
    void save_delegatesToSpringDataRepository() {
        OhlcvDocument candle = OhlcvDocument.create(
                "BTCUSDT",
                Instant.parse("2026-05-10T10:00:00Z"),
                new BigDecimal("65000")
        );

        adapter.save(candle);

        verify(repository).save(candle);
    }
}
package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.OhlcvCandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MongoDbOhlcvRepositoryAdapterTest {

    @Mock
    private SpringDataOhlcvRepository repository;

    @InjectMocks
    private MongoDbOhlcvRepositoryAdapter adapter;

    @Test
    void save_maps_candle_to_document_and_delegates_to_repository() {
        OhlcvCandle candle = OhlcvCandle.create(
                "BTCUSDT",
                Instant.parse("2026-05-10T10:00:00Z"),
                new BigDecimal("65000")
        );

        adapter.save(candle);

        ArgumentCaptor<OhlcvDocument> captor = ArgumentCaptor.forClass(OhlcvDocument.class);
        verify(repository).save(captor.capture());
        OhlcvDocument doc = captor.getValue();
        assertThat(doc.id()).isNull();
        assertThat(doc.symbol()).isEqualTo("BTCUSDT");
        assertThat(doc.open()).isEqualByComparingTo("65000");
        assertThat(doc.close()).isEqualByComparingTo("65000");
        assertThat(doc.priceUpdateCount()).isEqualTo(1);
    }
}
package com.msj.marketdata.infrastructure.adapters.persistence.mongo;

import com.msj.marketdata.domain.PriceUpdate;
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
class MongoDbPriceTickRepositoryAdapterTest {

    @Mock
    private SpringDataPriceTickRepository repository;

    @InjectMocks
    private MongoDbPriceTickRepositoryAdapter adapter;

    @Test
    void save_mapsTickToDocumentAndPersists() {
        Instant now = Instant.now();
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), now);

        adapter.save(tick);

        ArgumentCaptor<PriceTickDocument> captor = ArgumentCaptor.forClass(PriceTickDocument.class);
        verify(repository).save(captor.capture());
        PriceTickDocument doc = captor.getValue();
        assertThat(doc.id()).isNull();
        assertThat(doc.symbol()).isEqualTo("BTCUSDT");
        assertThat(doc.price()).isEqualByComparingTo("65000.00");
        assertThat(doc.timestamp()).isEqualTo(now);
    }
}
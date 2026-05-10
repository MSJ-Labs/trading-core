package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.adapters.persistence.mongo.OhlcvDocument;
import com.msj.marketdata.infrastructure.ports.OhlcvRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OhlcvAggregatorTest {

    @Mock
    private OhlcvRepository ohlcvRepository;

    @InjectMocks
    private OhlcvAggregator aggregator;

    private static final Instant T0 = Instant.parse("2026-05-10T10:00:00Z"); // minute boundary
    private static final Instant T1 = Instant.parse("2026-05-10T10:00:30Z"); // same minute
    private static final Instant T2 = Instant.parse("2026-05-10T10:01:00Z"); // next minute

    @Test
    void firstTick_opensCandle_doesNotFlush() {
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65000"), T0));

        verifyNoInteractions(ohlcvRepository);
        OhlcvDocument candle = aggregator.currentCandle("BTCUSDT").orElseThrow();
        assertThat(candle.open()).isEqualByComparingTo("65000");
        assertThat(candle.high()).isEqualByComparingTo("65000");
        assertThat(candle.low()).isEqualByComparingTo("65000");
        assertThat(candle.close()).isEqualByComparingTo("65000");
        assertThat(candle.priceUpdateCount()).isEqualTo(1);
    }

    @Test
    void sameBucket_updatesHighLowClose() {
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65000"), T0));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("66000"), T1));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("64500"), T1));

        verifyNoInteractions(ohlcvRepository);
        OhlcvDocument candle = aggregator.currentCandle("BTCUSDT").orElseThrow();
        assertThat(candle.open()).isEqualByComparingTo("65000");
        assertThat(candle.high()).isEqualByComparingTo("66000");
        assertThat(candle.low()).isEqualByComparingTo("64500");
        assertThat(candle.close()).isEqualByComparingTo("64500");
        assertThat(candle.priceUpdateCount()).isEqualTo(3);
    }

    @Test
    void newBucket_flushesCompletedCandle() {
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65000"), T0));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("66000"), T1));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65500"), T2));

        ArgumentCaptor<OhlcvDocument> captor = ArgumentCaptor.forClass(OhlcvDocument.class);
        verify(ohlcvRepository).save(captor.capture());
        OhlcvDocument flushed = captor.getValue();
        assertThat(flushed.open()).isEqualByComparingTo("65000");
        assertThat(flushed.high()).isEqualByComparingTo("66000");
        assertThat(flushed.low()).isEqualByComparingTo("65000");
        assertThat(flushed.close()).isEqualByComparingTo("66000");
        assertThat(flushed.priceUpdateCount()).isEqualTo(2);

        OhlcvDocument newCandle = aggregator.currentCandle("BTCUSDT").orElseThrow();
        assertThat(newCandle.open()).isEqualByComparingTo("65500");
        assertThat(newCandle.openTime()).isEqualTo(T2);
    }

    @Test
    void differentSymbols_aggregatedIndependently() {
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65000"), T0));
        aggregator.onTick(new PriceUpdate("ETHUSDT", new BigDecimal("3000"), T0));

        verifyNoInteractions(ohlcvRepository);
        assertThat(aggregator.currentCandle("BTCUSDT")).isPresent();
        assertThat(aggregator.currentCandle("ETHUSDT")).isPresent();
        assertThat(aggregator.currentCandle("BTCUSDT").orElseThrow().open())
                .isEqualByComparingTo("65000");
        assertThat(aggregator.currentCandle("ETHUSDT").orElseThrow().open())
                .isEqualByComparingTo("3000");
    }

    @Test
    void minuteBucketAlignment_truncatesToMinute() {
        // 10:00:45 and 10:00:59 are in the same 1m bucket as 10:00:00
        Instant midMinute = Instant.parse("2026-05-10T10:00:45Z");
        Instant endMinute = Instant.parse("2026-05-10T10:00:59Z");

        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65000"), T0));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65100"), midMinute));
        aggregator.onTick(new PriceUpdate("BTCUSDT", new BigDecimal("65200"), endMinute));

        verifyNoInteractions(ohlcvRepository);
        assertThat(aggregator.currentCandle("BTCUSDT").orElseThrow().priceUpdateCount()).isEqualTo(3);
    }
}
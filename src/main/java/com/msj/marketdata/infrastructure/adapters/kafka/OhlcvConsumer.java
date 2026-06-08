package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.OhlcvAggregationUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OhlcvConsumer {

    private final OhlcvAggregationUseCase ohlcvAggregationUseCase;

    @KafkaListener(topics = PriceTickProducer.TOPIC, groupId = "ohlcv-aggregator")
    public void consume(PriceUpdate tick) {
        ohlcvAggregationUseCase.onTick(tick);
        log.debug("Aggregated tick: {} = {}", tick.symbol(), tick.price());
    }
}
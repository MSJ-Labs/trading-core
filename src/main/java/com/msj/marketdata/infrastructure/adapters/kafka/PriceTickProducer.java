package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceTickPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceTickProducer implements PriceTickPublisher {

    static final String TOPIC = "market.price.updated";

    private final KafkaTemplate<String, PriceUpdate> kafkaTemplate;

    @Override
    public void publish(PriceUpdate tick) {
        kafkaTemplate.send(TOPIC, tick.symbol(), tick)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish tick for {}: {}", tick.symbol(), ex.getMessage());
                    }
                });
    }
}
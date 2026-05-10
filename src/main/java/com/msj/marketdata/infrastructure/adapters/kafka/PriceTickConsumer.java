package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceTickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceTickConsumer {

    private final PriceTickRepository priceTickRepository;

    @KafkaListener(topics = PriceTickProducer.TOPIC, groupId = "tick-persister")
    public void consume(PriceUpdate tick) {
        priceTickRepository.save(tick);
        log.debug("Persisted tick: {} = {}", tick.symbol(), tick.price());
    }
}
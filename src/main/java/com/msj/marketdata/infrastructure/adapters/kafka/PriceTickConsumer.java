package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.PersistPriceTickCommand;
import com.msj.marketdata.application.command.PersistPriceTickUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceTickConsumer {

    private final PersistPriceTickUseCase persistPriceTickUseCase;

    @KafkaListener(topics = PriceTickProducer.TOPIC, groupId = "tick-persister")
    public void consume(PriceUpdate tick) {
        persistPriceTickUseCase.handle(new PersistPriceTickCommand(tick.symbol(), tick.price(), tick.timestamp()));
        log.debug("Persisted tick: {} = {}", tick.symbol(), tick.price());
    }
}
package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.application.command.PersistPriceTickCommand;
import com.msj.marketdata.application.command.PersistPriceTickUseCase;
import com.msj.marketdata.domain.PriceUpdate;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceTickConsumer {

    private final PersistPriceTickUseCase persistPriceTickUseCase;
    private final MeterRegistry meterRegistry;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    @KafkaListener(topics = PriceTickProducer.TOPIC, groupId = "tick-persister")
    public void consume(PriceUpdate tick) {
        persistPriceTickUseCase.handle(new PersistPriceTickCommand(tick.symbol(), tick.price(), tick.timestamp()));
        log.debug("Persisted tick: {} = {}", tick.symbol(), tick.price());
    }

    @DltHandler
    public void handleDlt(PriceUpdate tick,
                           @Header(KafkaHeaders.ORIGINAL_OFFSET) long offset,
                           @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("Tick persistence exhausted retries, sent to DLT: symbol={} offset={} reason={}",
                tick.symbol(), offset, exceptionMessage);
        meterRegistry.counter("marketdata.kafka.dlt", "consumer", "tick-persister").increment();
    }
}
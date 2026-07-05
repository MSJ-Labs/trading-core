package com.msj.marketdata.infrastructure.adapters.events;

import com.msj.marketdata.domain.events.PriceUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Extension point for future consumers of tracked-coin price changes
 * (alerting rule evaluation, portfolio P&L recalculation).
 * Async so a slow/failing listener never blocks tick processing.
 */
@Slf4j
@Component
public class PriceUpdatedEventListener {

    @Async
    @EventListener
    public void onPriceUpdated(PriceUpdatedEvent event) {
        log.debug("Price updated: {} ({}) = {} at {}",
                event.coinId(), event.symbol(), event.price(), event.occurredOn());
    }
}
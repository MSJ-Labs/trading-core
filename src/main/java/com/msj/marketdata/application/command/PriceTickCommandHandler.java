package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceBroadcaster;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import com.msj.marketdata.infrastructure.ports.PriceTickPublisher;
import com.msj.shared.domain.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceTickCommandHandler implements PriceTickUseCase {

    private final PriceCache priceCache;
    private final PriceBroadcaster priceBroadcaster;
    private final PriceTickPublisher priceTickPublisher;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public void handle(PriceTickCommand command) {
        PriceUpdate tick = new PriceUpdate(command.symbol(), command.price(), command.timestamp());
        priceBroadcaster.broadcast(tick);
        priceTickPublisher.publish(tick);
        applyToTrackedCoins(tick);
    }

    private void applyToTrackedCoins(PriceUpdate tick) {
        for (CoinPrice coin : priceCache.findBySymbol(tick.symbol())) {
            CoinPrice updated = coin.withUpdatedPrice(tick);
            priceCache.putCoinPrice(updated);
            domainEventPublisher.publish(updated.pullDomainEvents());
        }
    }
}
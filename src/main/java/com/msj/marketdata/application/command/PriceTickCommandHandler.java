package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceBroadcaster;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import com.msj.marketdata.infrastructure.ports.PriceTickPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceTickCommandHandler implements PriceTickUseCase {

    private final PriceCache priceCache;
    private final PriceBroadcaster priceBroadcaster;
    private final PriceTickPublisher priceTickPublisher;

    @Override
    public void handle(PriceTickCommand command) {
        PriceUpdate tick = new PriceUpdate(command.symbol(), command.price(), command.timestamp());
        priceCache.updatePrice(tick);
        priceBroadcaster.broadcast(tick);
        priceTickPublisher.publish(tick);
    }
}
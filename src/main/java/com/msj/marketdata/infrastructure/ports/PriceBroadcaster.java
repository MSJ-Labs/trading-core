package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.PriceUpdate;

public interface PriceBroadcaster {
    void broadcast(PriceUpdate priceUpdate);
}
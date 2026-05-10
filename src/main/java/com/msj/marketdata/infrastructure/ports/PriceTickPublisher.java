package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.PriceUpdate;

public interface PriceTickPublisher {
    void publish(PriceUpdate tick);
}
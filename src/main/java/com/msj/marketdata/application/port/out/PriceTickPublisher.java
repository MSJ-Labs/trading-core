package com.msj.marketdata.application.port.out;

import com.msj.marketdata.domain.PriceUpdate;

public interface PriceTickPublisher {
    void publish(PriceUpdate tick);
}
package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.PriceUpdate;

public interface PriceTickRepository {
    void save(PriceUpdate tick);
}
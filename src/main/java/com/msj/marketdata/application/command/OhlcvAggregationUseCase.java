package com.msj.marketdata.application.command;

import com.msj.marketdata.domain.PriceUpdate;

public interface OhlcvAggregationUseCase {
    void onTick(PriceUpdate tick);
}
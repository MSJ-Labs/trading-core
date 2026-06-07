package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.OhlcvCandle;

public interface OhlcvRepository {
    void save(OhlcvCandle candle);
}
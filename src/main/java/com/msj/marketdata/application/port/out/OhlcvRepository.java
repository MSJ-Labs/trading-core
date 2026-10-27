package com.msj.marketdata.application.port.out;

import com.msj.marketdata.domain.OhlcvCandle;

public interface OhlcvRepository {
    void save(OhlcvCandle candle);
}
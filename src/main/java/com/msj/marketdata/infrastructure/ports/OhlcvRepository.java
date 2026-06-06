package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.infrastructure.adapters.persistence.mongo.OhlcvDocument;

public interface OhlcvRepository {
    void save(OhlcvDocument candle);
}
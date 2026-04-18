package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.CoinPrice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PriceCache {
    List<CoinPrice> getTopCoins();
    void putTopCoins(List<CoinPrice> coins);
    Optional<CoinPrice> getCoinPrice(String coinId);
    void putCoinPrice(CoinPrice coinPrice);
    void updatePrice(String symbol, BigDecimal price, Instant timestamp);
}
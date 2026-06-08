package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.domain.PriceUpdate;

import java.util.List;
import java.util.Optional;

public interface PriceCache {
    List<CoinPrice> getTopCoins();
    void putTopCoins(List<CoinPrice> coins);
    Optional<CoinPrice> getCoinPrice(String coinId);
    void putCoinPrice(CoinPrice coinPrice);
    void updatePrice(PriceUpdate tick);
}
package com.msj.marketdata.infrastructure.ports;

import com.msj.marketdata.domain.CoinPrice;

import java.util.List;
import java.util.Optional;

public interface MarketDataProvider {
    List<CoinPrice> fetchTopCoins(int limit);
    Optional<CoinPrice> fetchCoinPrice(String coinId);
}
package com.msj.marketdata.application.query;

import com.msj.marketdata.domain.CoinNotFoundException;
import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.infrastructure.ports.MarketDataProvider;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCoinPriceQueryHandler {

    private final PriceCache priceCache;
    private final MarketDataProvider marketDataProvider;

    public CoinPrice handle(GetCoinPriceQuery query) {
        return priceCache.getCoinPrice(query.coinId())
                .orElseGet(() -> {
                    CoinPrice price = marketDataProvider.fetchCoinPrice(query.coinId())
                            .orElseThrow(() -> new CoinNotFoundException(query.coinId()));
                    priceCache.putCoinPrice(price);
                    return price;
                });
    }
}
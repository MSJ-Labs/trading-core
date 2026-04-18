package com.msj.marketdata.application.query;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.infrastructure.ports.MarketDataProvider;
import com.msj.marketdata.infrastructure.ports.PriceCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopCoinsQueryHandler {

    private final PriceCache priceCache;
    private final MarketDataProvider marketDataProvider;

    public List<CoinPrice> handle(GetTopCoinsQuery query) {
        List<CoinPrice> cached = priceCache.getTopCoins();
        if (!cached.isEmpty()) {
            return cached.stream().limit(query.limit()).toList();
        }
        List<CoinPrice> coins = marketDataProvider.fetchTopCoins(query.limit());
        priceCache.putTopCoins(coins);
        return coins;
    }
}
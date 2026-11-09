package com.msj.marketdata.application.command;

import com.msj.marketdata.application.port.out.MarketDataProvider;
import com.msj.marketdata.application.port.out.PriceCache;
import com.msj.marketdata.domain.CoinPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTopCoinsCommandHandler implements RefreshTopCoinsUseCase {

    private final MarketDataProvider marketDataProvider;
    private final PriceCache priceCache;

    @Override
    public void refresh(int limit) {
        List<CoinPrice> coins = marketDataProvider.fetchTopCoins(limit);
        priceCache.putTopCoins(coins);
    }
}
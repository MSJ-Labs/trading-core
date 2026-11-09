package com.msj.marketdata.application.command;

import com.msj.marketdata.application.port.out.MarketDataProvider;
import com.msj.marketdata.application.port.out.PriceCache;
import com.msj.marketdata.domain.CoinPrice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTopCoinsCommandHandlerTest {

    @Mock
    private MarketDataProvider marketDataProvider;

    @Mock
    private PriceCache priceCache;

    @InjectMocks
    private RefreshTopCoinsCommandHandler handler;

    @Test
    void refresh_fetches_requested_limit_and_stores_result_in_cache() {
        List<CoinPrice> coins = List.of(
                CoinPrice.builder().id("bitcoin").symbol("BTC").name("Bitcoin").build());
        when(marketDataProvider.fetchTopCoins(250)).thenReturn(coins);

        handler.refresh(250);

        verify(priceCache).putTopCoins(coins);
    }
}
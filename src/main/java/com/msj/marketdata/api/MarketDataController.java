package com.msj.marketdata.api;

import com.msj.marketdata.api.dto.CoinPriceResponse;
import com.msj.marketdata.application.query.GetCoinPriceQuery;
import com.msj.marketdata.application.query.GetCoinPriceQueryHandler;
import com.msj.marketdata.application.query.GetTopCoinsQuery;
import com.msj.marketdata.application.query.GetTopCoinsQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final GetTopCoinsQueryHandler getTopCoinsQueryHandler;
    private final GetCoinPriceQueryHandler getCoinPriceQueryHandler;

    @GetMapping("/coins")
    public ResponseEntity<List<CoinPriceResponse>> getTopCoins(
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<CoinPriceResponse> coins = getTopCoinsQueryHandler.handle(new GetTopCoinsQuery(limit))
                .stream()
                .map(CoinPriceResponse::from)
                .toList();
        return ResponseEntity.ok(coins);
    }

    @GetMapping("/coins/{coinId}")
    public ResponseEntity<CoinPriceResponse> getCoinPrice(@PathVariable String coinId) {
        return ResponseEntity.ok(
                CoinPriceResponse.from(getCoinPriceQueryHandler.handle(new GetCoinPriceQuery(coinId))));
    }
}

package com.msj.marketdata.infrastructure.adapters.coingecko;

import com.msj.marketdata.domain.CoinPrice;
import com.msj.marketdata.infrastructure.ports.MarketDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoinGeckoAdapter implements MarketDataProvider {

    private final RestClient coinGeckoRestClient;

    @Override
    public List<CoinPrice> fetchTopCoins(int limit) {
        log.debug("Fetching top {} coins from CoinGecko", limit);
        CoinGeckoMarketResponse[] response = coinGeckoRestClient.get()
                .uri("/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page={limit}&page=1&sparkline=false", limit)
                .retrieve()
                .body(CoinGeckoMarketResponse[].class);
        if (response == null) return List.of();
        return Arrays.stream(response).map(this::toDomain).toList();
    }

    @Override
    public Optional<CoinPrice> fetchCoinPrice(String coinId) {
        log.debug("Fetching price for {} from CoinGecko", coinId);
        CoinGeckoMarketResponse[] response = coinGeckoRestClient.get()
                .uri("/api/v3/coins/markets?vs_currency=usd&ids={coinId}&order=market_cap_desc&per_page=1&page=1", coinId)
                .retrieve()
                .body(CoinGeckoMarketResponse[].class);
        if (response == null || response.length == 0) return Optional.empty();
        return Optional.of(toDomain(response[0]));
    }

    private CoinPrice toDomain(CoinGeckoMarketResponse r) {
        return new CoinPrice(
                r.id(),
                r.symbol().toUpperCase(),
                r.name(),
                r.currentPrice(),
                r.priceChangePercentage24h(),
                r.marketCap(),
                r.totalVolume(),
                r.lastUpdated() != null ? Instant.parse(r.lastUpdated()) : Instant.now()
        );
    }
}

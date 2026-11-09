package com.msj.marketdata.infrastructure.adapters.scheduler;

import com.msj.marketdata.application.command.RefreshTopCoinsUseCase;
import com.msj.marketdata.infrastructure.adapters.coingecko.CoinGeckoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: triggers the CoinGecko top-coins refresh every 60 s. ShedLock (Redis) makes
 * sure only one replica calls CoinGecko per tick; {@code lockAtLeastFor} also stops a replica
 * with a drifting clock from running again right after another one finished.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "marketdata.refresh.enabled", havingValue = "true", matchIfMissing = true)
class TopCoinsRefreshScheduler {

    private final RefreshTopCoinsUseCase refreshTopCoins;
    private final CoinGeckoProperties properties;

    @Scheduled(fixedRateString = "${marketdata.refresh.interval:60s}", initialDelayString = "${marketdata.refresh.initial-delay:5s}")
    @SchedulerLock(name = "marketdata-refresh-top-coins", lockAtMostFor = "55s", lockAtLeastFor = "30s")
    void refresh() {
        try {
            refreshTopCoins.refresh(properties.topCoinsLimit());
        } catch (RuntimeException e) {
            // Keep the schedule alive (e.g. CoinGecko 429): next tick retries, cached prices stay served.
            log.warn("Top coins refresh failed: {}", e.toString());
        }
    }
}
package com.msj.marketdata.infrastructure.adapters.scheduler;

import com.msj.marketdata.application.command.RefreshTopCoinsUseCase;
import com.msj.marketdata.infrastructure.adapters.coingecko.CoinGeckoProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopCoinsRefreshSchedulerTest {

    private final CoinGeckoProperties properties = new CoinGeckoProperties("http://localhost", "", 250);

    @Mock
    private RefreshTopCoinsUseCase refreshTopCoins;

    @Test
    void refresh_triggers_use_case_with_configured_limit() {
        new TopCoinsRefreshScheduler(refreshTopCoins, properties).refresh();

        verify(refreshTopCoins).refresh(250);
    }

    @Test
    void refresh_swallows_provider_failure_so_the_schedule_keeps_running() {
        doThrow(new IllegalStateException("429 Too Many Requests")).when(refreshTopCoins).refresh(250);

        assertThatCode(() -> new TopCoinsRefreshScheduler(refreshTopCoins, properties).refresh())
                .doesNotThrowAnyException();
    }
}
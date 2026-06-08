package com.msj.marketdata.infrastructure.adapters.stomp;

import com.msj.marketdata.domain.PriceUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StompPriceBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private StompPriceBroadcaster broadcaster;

    @Test
    void broadcast_sends_to_topic_prices() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", BigDecimal.valueOf(50_000), Instant.now());

        broadcaster.broadcast(tick);

        verify(messagingTemplate).convertAndSend("/topic/prices", tick);
    }
}
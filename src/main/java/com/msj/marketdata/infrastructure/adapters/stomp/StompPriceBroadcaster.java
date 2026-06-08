package com.msj.marketdata.infrastructure.adapters.stomp;

import com.msj.marketdata.domain.PriceUpdate;
import com.msj.marketdata.infrastructure.ports.PriceBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompPriceBroadcaster implements PriceBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcast(PriceUpdate priceUpdate) {
        messagingTemplate.convertAndSend("/topic/prices", priceUpdate);
    }
}
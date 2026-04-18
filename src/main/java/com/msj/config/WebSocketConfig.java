package com.msj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures a STOMP-over-WebSocket message broker.
 * <p>
 * Architecture:
 *   Browser  ──(SockJS/WS)──▶  /ws  ──▶  Spring STOMP broker
 *   Backend  ──────────────────────────▶  SimpMessagingTemplate.convertAndSend("/topic/prices", ...)
 *   Browser  ◀──────────────────────────  SUBSCRIBE /topic/prices
 * </p>
 * <p>
 * The Binance WebSocket client (BinanceWebSocketClient) pushes price updates onto /topic/prices,
 * and every connected browser receives them in real-time.
 * </p>
 * Two layers here:
 *   - Simple in-process broker (enableSimpleBroker): handles /topic/** fan-out entirely in memory.
 *     Replace with a RabbitMQ or ActiveMQ relay at Layer 5 when you need multi-instance support.
 *   - SockJS fallback (withSockJS): transparently downgrades to long-polling for proxies or
 *     corporate networks that block WebSocket upgrades.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * /topic/**  → in-memory fan-out broker (pub/sub, one message → all subscribers)
     * /app/**    → messages sent from clients route through @MessageMapping handlers first
     *              (not used yet, reserved for Layer 6 when clients send commands over WS)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the WebSocket handshake endpoint.
     * setAllowedOriginPatterns("*") is intentionally permissive in dev.
     * Restrict to the actual frontend origin (e.g. "http://localhost:5173") before going to prod.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

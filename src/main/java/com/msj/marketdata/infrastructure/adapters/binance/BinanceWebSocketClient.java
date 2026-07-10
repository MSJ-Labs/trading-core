package com.msj.marketdata.infrastructure.adapters.binance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msj.marketdata.application.command.PriceTickCommand;
import com.msj.marketdata.application.command.PriceTickUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceWebSocketClient {

    private final BinanceProperties properties;
    private final ObjectMapper objectMapper;
    private final PriceTickUseCase priceTickUseCase;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();

    /**
     * Opens a combined stream on Binance: one WebSocket connection, multiple symbols.
     * Format: wss://...?streams=btcusdt@miniTicker/ethusdt@miniTicker/...
     * Each miniTicker event arrives ~every second and carries symbol + last price.
     * Connection failures are logged but don't crash startup — prices just won't stream.
     */
    @PostConstruct
    public void connect() {
        String streams = properties.defaultSymbols().stream()
                .map(s -> s.toLowerCase() + "@miniTicker")
                .collect(Collectors.joining("/"));
        String url = properties.websocketUrl() + "?streams=" + streams;
        log.info("Connecting to Binance WebSocket streams: {}", streams);

        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(url), new BinanceListener())
                .thenAccept(ws -> {
                    webSocketRef.set(ws);
                    log.info("Binance WebSocket connected");
                })
                .exceptionally(ex -> {
                    log.warn("Binance WebSocket connection failed: {}", ex.getMessage());
                    return null;
                });
    }

    @PreDestroy
    public void disconnect() {
        WebSocket ws = webSocketRef.get();
        if (ws != null && !ws.isOutputClosed()) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
        httpClient.close();
    }

    private void handleMessage(String text) {
        try {
            JsonNode root = objectMapper.readTree(text);
            JsonNode data = root.has("data") ? root.get("data") : root;
            if (data.isArray()) {
                data.forEach(this::processTicker);
            } else {
                processTicker(data);
            }
        } catch (Exception e) {
            log.warn("Failed to parse Binance message: {}", e.getMessage());
        }
    }

    private void processTicker(JsonNode node) {
        try {
            BinanceMiniTickerMessage ticker = objectMapper.treeToValue(node, BinanceMiniTickerMessage.class);
            if (ticker.closePrice() == null) return;
            Instant ts = ticker.eventTime() != null ? Instant.ofEpochMilli(ticker.eventTime()) : Instant.now();
            priceTickUseCase.handle(new PriceTickCommand(ticker.symbol(), ticker.closePrice(), ts));
        } catch (Exception e) {
            log.warn("Failed to process ticker node: {}", e.getMessage());
        }
    }

    private class BinanceListener implements WebSocket.Listener {

        // Java's WebSocket API delivers large frames in chunks (last=false until the final chunk).
        // We buffer all chunks and process the complete JSON only when last=true.
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            ws.request(1);
            if (last) {
                handleMessage(buffer.toString());
                buffer.setLength(0);
            }
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            log.error("Binance WebSocket error: {}", error.getMessage());
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            log.warn("Binance WebSocket closed [{} {}]", statusCode, reason);
            return null;
        }
    }
}
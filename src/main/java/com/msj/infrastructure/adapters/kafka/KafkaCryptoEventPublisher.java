package com.msj.infrastructure.adapters.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msj.domain.crypto.Crypto;
import com.msj.infrastructure.ports.crypto.CryptoEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka adapter for publishing crypto events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCryptoEventPublisher implements CryptoEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String CRYPTO_CREATED_TOPIC = "crypto-created";
    private static final String CRYPTO_UPDATED_TOPIC = "crypto-updated";
    private static final String CRYPTO_DELETED_TOPIC = "crypto-deleted";

    @Override
    public void publishCryptoCreated(Crypto crypto) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "CRYPTO_CREATED");
            event.put("crypto", crypto);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(CRYPTO_CREATED_TOPIC, crypto.getId().value(), message);
            log.info("Crypto created event published: {}", crypto.getId().value());
        } catch (Exception e) {
            log.error("Error publishing crypto created event", e);
        }
    }

    @Override
    public void publishCryptoUpdated(Crypto crypto) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "CRYPTO_UPDATED");
            event.put("crypto", crypto);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(CRYPTO_UPDATED_TOPIC, crypto.getId().value(), message);
            log.info("Crypto updated event published: {}", crypto.getId().value());
        } catch (Exception e) {
            log.error("Error publishing crypto updated event", e);
        }
    }

    @Override
    public void publishCryptoDeleted(String cryptoId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "CRYPTO_DELETED");
            event.put("cryptoId", cryptoId);
            event.put("timestamp", System.currentTimeMillis());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(CRYPTO_DELETED_TOPIC, cryptoId, message);
            log.info("Crypto deleted event published: {}", cryptoId);
        } catch (Exception e) {
            log.error("Error publishing crypto deleted event", e);
        }
    }
}


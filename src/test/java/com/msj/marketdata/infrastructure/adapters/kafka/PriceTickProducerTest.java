package com.msj.marketdata.infrastructure.adapters.kafka;

import com.msj.marketdata.domain.PriceUpdate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.msj.marketdata.infrastructure.adapters.kafka.PriceTickProducer.TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceTickProducerTest {

    @Mock
    private KafkaTemplate<String, PriceUpdate> kafkaTemplate;

    @InjectMocks
    private PriceTickProducer producer;

    @Test
    void publish_sendsToCorrectTopicWithSymbolAsKey() {
        PriceUpdate tick = new PriceUpdate("BTCUSDT", new BigDecimal("65000.00"), Instant.now());
        ProducerRecord<String, PriceUpdate> producerRecord = new ProducerRecord<>(TOPIC, "BTCUSDT", tick);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(TOPIC, 0), 0, 0, 0, 0, 0);
        when(kafkaTemplate.send(TOPIC, "BTCUSDT", tick))
                .thenReturn(CompletableFuture.completedFuture(new SendResult<>(producerRecord, metadata)));

        producer.publish(tick);

        verify(kafkaTemplate).send(TOPIC, "BTCUSDT", tick);
    }

    @Test
    void publish_doesNotThrowWhenKafkaFails() {
        PriceUpdate tick = new PriceUpdate("ETHUSDT", new BigDecimal("3000.00"), Instant.now());
        CompletableFuture<SendResult<String, PriceUpdate>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(TOPIC, "ETHUSDT", tick)).thenReturn(failed);

        assertThatNoException().isThrownBy(() -> producer.publish(tick));
    }
}
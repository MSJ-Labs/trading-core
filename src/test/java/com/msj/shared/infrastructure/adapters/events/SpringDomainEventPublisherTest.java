package com.msj.shared.infrastructure.adapters.events;

import com.msj.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    private record FakeEvent(Instant occurredOn) implements DomainEvent {}

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SpringDomainEventPublisher publisher;

    @Test
    void publish_forwardsEachEventIndividually() {
        FakeEvent first = new FakeEvent(Instant.parse("2026-06-07T10:00:00Z"));
        FakeEvent second = new FakeEvent(Instant.parse("2026-06-07T10:01:00Z"));

        publisher.publish(List.of(first, second));

        verify(applicationEventPublisher).publishEvent(first);
        verify(applicationEventPublisher).publishEvent(second);
    }

    @Test
    void publish_withEmptyList_doesNothing() {
        publisher.publish(List.of());

        verifyNoInteractions(applicationEventPublisher);
    }
}
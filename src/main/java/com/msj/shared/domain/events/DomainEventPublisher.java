package com.msj.shared.domain.events;

import com.msj.shared.domain.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<DomainEvent> events);
}
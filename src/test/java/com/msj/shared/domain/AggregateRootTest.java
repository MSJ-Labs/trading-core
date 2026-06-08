package com.msj.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    record TestEvent(String name, Instant occurredOn) implements DomainEvent {}

    static class TestAggregate extends AggregateRoot<String> {
        TestAggregate(String id) { super(id); }
        void raiseEvent(DomainEvent event) { registerEvent(event); }
    }

    @Test
    void id_returns_constructor_value() {
        assertThat(new TestAggregate("abc").id()).isEqualTo("abc");
    }

    @Test
    void equals_and_hashCode_are_identity_based() {
        TestAggregate a = new TestAggregate("x");
        TestAggregate b = new TestAggregate("x");
        TestAggregate c = new TestAggregate("y");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void equals_returns_false_for_null_and_different_type() {
        TestAggregate a = new TestAggregate("x");
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("x");
    }

    @Test
    void pullDomainEvents_returns_registered_events_then_clears() {
        Instant now = Instant.now();
        TestAggregate agg = new TestAggregate("id");
        agg.raiseEvent(new TestEvent("event-one", now));
        agg.raiseEvent(new TestEvent("event-two", now));

        List<DomainEvent> first = agg.pullDomainEvents();
        assertThat(first).containsExactly(new TestEvent("event-one", now), new TestEvent("event-two", now));

        assertThat(agg.pullDomainEvents()).isEmpty();
    }

    @Test
    void pullDomainEvents_returns_empty_when_no_events_registered() {
        assertThat(new TestAggregate("id").pullDomainEvents()).isEmpty();
    }
}
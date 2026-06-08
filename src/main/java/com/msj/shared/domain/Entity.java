package com.msj.shared.domain;

import lombok.experimental.SuperBuilder;

import java.util.Objects;

@SuperBuilder
public abstract class Entity<ID> {

    private final ID id;

    protected Entity(ID id) {
        this.id = id;
    }

    public ID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
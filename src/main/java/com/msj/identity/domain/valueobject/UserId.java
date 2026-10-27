package com.msj.identity.domain.valueobject;

import com.msj.shared.domain.ValueObject;

import java.util.UUID;

/** The identity of a user: the {@code sub} Keycloak gives them. Other contexts define their own type for it. */
public record UserId(UUID value) implements ValueObject {

    public UserId {
        if (value == null) throw new IllegalArgumentException("UserId cannot be null");
    }
}
package com.msj.identity.application.result;

import com.msj.identity.domain.valueobject.UserId;

import java.util.Set;

/** The caller of the current use case: who they are and the application roles they hold (e.g. USER, ADMIN). */
public record CurrentUser(UserId id, Set<String> roles) {

    public CurrentUser {
        if (id == null) throw new IllegalArgumentException("Id is required");
        roles = Set.copyOf(roles);
    }
}
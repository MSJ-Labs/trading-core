package com.msj.identity.api.dto;

import com.msj.identity.application.result.CurrentUser;

import java.util.List;

public record CurrentIdentityResponse(String userId, List<String> roles) {

    public static CurrentIdentityResponse from(CurrentUser user) {
        return new CurrentIdentityResponse(user.id().value().toString(), user.roles().stream().sorted().toList());
    }
}
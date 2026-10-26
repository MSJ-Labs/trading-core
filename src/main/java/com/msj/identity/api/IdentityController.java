package com.msj.identity.api;

import com.msj.identity.api.dto.CurrentIdentityResponse;
import com.msj.identity.application.query.GetCurrentIdentityQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Identity comes from the authenticated principal inside the handler; the request carries no user id. */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class IdentityController {

    private final GetCurrentIdentityQueryHandler getCurrentIdentity;

    @GetMapping
    public CurrentIdentityResponse me() {
        return CurrentIdentityResponse.from(getCurrentIdentity.handle());
    }
}
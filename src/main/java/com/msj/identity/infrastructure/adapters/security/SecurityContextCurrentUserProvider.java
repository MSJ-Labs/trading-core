package com.msj.identity.infrastructure.adapters.security;

import com.msj.identity.application.port.out.CurrentUserProvider;
import com.msj.identity.application.result.CurrentUser;
import com.msj.identity.domain.valueobject.UserId;
import com.msj.shared.infrastructure.security.AuthenticatedPrincipal;
import com.msj.shared.infrastructure.security.CurrentAuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Translates the authenticated principal into the identity context's own types. It fails closed: anything that cannot
 * say who the caller is, the anonymous user included, is refused rather than guessed.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    private final CurrentAuthenticatedPrincipal currentPrincipal;

    @Override
    public CurrentUser currentUser() {
        AuthenticatedPrincipal principal = currentPrincipal.get()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No authenticated user."));
        return new CurrentUser(new UserId(principal.userId()), principal.roles());
    }
}
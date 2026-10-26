package com.msj.identity.infrastructure.adapters.security;

import com.msj.identity.application.result.CurrentUser;
import com.msj.shared.infrastructure.security.AuthenticatedPrincipal;
import com.msj.shared.infrastructure.security.CurrentAuthenticatedPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextCurrentUserProviderTest {

    @Mock
    private CurrentAuthenticatedPrincipal currentPrincipal;

    @Mock
    private AuthenticatedPrincipal principal;

    @InjectMocks
    private SecurityContextCurrentUserProvider provider;

    @Test
    void currentUser_translatesThePrincipalIntoTheContextsOwnTypes() {
        UUID id = UUID.randomUUID();
        when(principal.userId()).thenReturn(id);
        when(principal.roles()).thenReturn(Set.of("USER", "ADMIN"));
        when(currentPrincipal.get()).thenReturn(Optional.of(principal));

        CurrentUser user = provider.currentUser();

        assertThat(user.id().value()).isEqualTo(id);
        assertThat(user.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void currentUser_refusesWhenNobodyIsAuthenticated() {
        when(currentPrincipal.get()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.currentUser())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
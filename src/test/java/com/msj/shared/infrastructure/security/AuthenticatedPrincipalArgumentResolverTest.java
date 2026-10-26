package com.msj.shared.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedPrincipalArgumentResolverTest {

    private static final UUID USER_ID = UUID.fromString("3f1c9a52-0000-4000-8000-000000000001");

    private final AuthenticatedPrincipalArgumentResolver resolver = new AuthenticatedPrincipalArgumentResolver(new CurrentAuthenticatedPrincipal());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsParameter_acceptsPrincipalAndOptionalPrincipalOnly() throws Exception {
        assertThat(resolver.supportsParameter(parameter("required"))).isTrue();
        assertThat(resolver.supportsParameter(parameter("optional"))).isTrue();
        assertThat(resolver.supportsParameter(parameter("nullable"))).isTrue();
        assertThat(resolver.supportsParameter(parameter("unrelated"))).isFalse();
    }

    @Test
    void resolveArgument_returnsPrincipalForJwtAuthentication() throws Exception {
        authenticateAsUser();

        Object resolved = resolver.resolveArgument(parameter("required"), null, null, null);

        assertThat(resolved).isInstanceOfSatisfying(AuthenticatedPrincipal.class, p -> {
            assertThat(p.userId()).isEqualTo(USER_ID);
            assertThat(p.hasRole("USER")).isTrue();
        });
    }

    @Test
    void resolveArgument_wrapsPrincipalInOptionalWhenAuthenticated() throws Exception {
        authenticateAsUser();

        Object resolved = resolver.resolveArgument(parameter("optional"), null, null, null);

        assertThat(resolved).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isPresent());
    }

    @Test
    void resolveArgument_refusesAnonymousWhenPrincipalIsRequired() throws Exception {
        authenticateAsAnonymous();
        MethodParameter required = parameter("required");

        assertThatThrownBy(() -> resolver.resolveArgument(required, null, null, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void resolveArgument_refusesWhenThereIsNoAuthenticationAtAll() throws Exception {
        MethodParameter required = parameter("required");

        assertThatThrownBy(() -> resolver.resolveArgument(required, null, null, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void resolveArgument_returnsEmptyOptionalForAnonymous() throws Exception {
        authenticateAsAnonymous();

        assertThat(resolver.resolveArgument(parameter("optional"), null, null, null)).isEqualTo(Optional.empty());
    }

    @Test
    void resolveArgument_returnsNullForAnonymousWhenParameterIsNullable() throws Exception {
        authenticateAsAnonymous();

        assertThat(resolver.resolveArgument(parameter("nullable"), null, null, null)).isNull();
    }

    private void authenticateAsUser() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(USER_ID.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private void authenticateAsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    }

    private MethodParameter parameter(String methodName) throws Exception {
        var method = Controller.class.getDeclaredMethod(methodName,
                switch (methodName) {
                    case "optional" -> Optional.class;
                    case "unrelated" -> String.class;
                    default -> AuthenticatedPrincipal.class;
                });
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private static class Controller {
        void required(AuthenticatedPrincipal principal) { /* signature only, read by reflection */ }
        void optional(Optional<AuthenticatedPrincipal> principal) { /* signature only, read by reflection */ }
        void nullable(@Nullable AuthenticatedPrincipal principal) { /* signature only, read by reflection */ }
        void unrelated(String value) { /* signature only, read by reflection */ }
    }
}
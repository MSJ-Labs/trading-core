package com.msj.identity.api;

import com.msj.config.SecurityConfig;
import com.msj.identity.application.query.GetCurrentIdentityQueryHandler;
import com.msj.identity.application.result.CurrentUser;
import com.msj.identity.domain.valueobject.UserId;
import com.msj.shared.infrastructure.security.CurrentAuthenticatedPrincipal;
import com.msj.shared.infrastructure.security.KeycloakJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IdentityController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, CurrentAuthenticatedPrincipal.class})
class IdentityControllerTest {

    private static final UUID USER_ID = UUID.fromString("3f1c9a52-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCurrentIdentityQueryHandler getCurrentIdentity;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void me_returnsTheCallersIdAndSortedRoles() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(USER_ID.toString())
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode("token")).thenReturn(jwt);
        when(getCurrentIdentity.handle()).thenReturn(new CurrentUser(new UserId(USER_ID), Set.of("USER", "ADMIN")));

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("USER"));
    }

    @Test
    void me_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }
}
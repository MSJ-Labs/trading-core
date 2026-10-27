package com.msj.config;

import com.msj.auth.infrastructure.security.JwtAuthenticationFilter;
import com.msj.shared.infrastructure.security.AuthenticatedPrincipal;
import com.msj.shared.infrastructure.security.CurrentAuthenticatedPrincipal;
import com.msj.shared.infrastructure.security.KeycloakJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The legacy JwtAuthenticationFilter is a @Component that @WebMvcTest would scan; it needs beans of the old auth BC.
@WebMvcTest(controllers = SecurityConfigTest.ProbeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, CurrentAuthenticatedPrincipal.class,
        SecurityConfigTest.ProbeController.class})
class SecurityConfigTest {

    private static final String USER_ID = "3f1c9a52-0000-4000-8000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // Legacy: SecurityConfig exposes an AuthenticationManager bean. Without a UserDetailsService it becomes its
    // own parent and a rejected token ends in a StackOverflowError. Remove with the legacy auth (B4).
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void publicRoute_isReachableWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/market/probe"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedRoute_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/probe/secured"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_returns401WhenTokenIsRejectedByTheDecoder() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("bad audience"));

        mockMvc.perform(get("/probe/secured").header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_acceptsValidToken() throws Exception {
        tokenWithRoles("user-token", "USER");

        mockMvc.perform(get("/probe/secured").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
    }

    @Test
    void adminRoute_returns403ForUserRole() throws Exception {
        tokenWithRoles("user-token", "USER");

        mockMvc.perform(get("/probe/admin").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoute_isAllowedForAdminRole() throws Exception {
        tokenWithRoles("admin-token", "USER", "ADMIN");

        mockMvc.perform(get("/probe/admin").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void controller_receivesAuthenticatedPrincipal() throws Exception {
        tokenWithRoles("user-token", "USER");

        mockMvc.perform(get("/probe/me").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(USER_ID));
    }

    private void tokenWithRoles(String tokenValue, String... roles) {
        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "RS256")
                .subject(USER_ID)
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode(tokenValue)).thenReturn(jwt);
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/v1/market/probe")
        String publicProbe() {
            return "public";
        }

        @GetMapping("/probe/secured")
        String secured() {
            return "secured";
        }

        @GetMapping("/probe/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String admin() {
            return "admin";
        }

        @GetMapping("/probe/me")
        String me(AuthenticatedPrincipal principal) {
            return principal.userId().toString();
        }
    }
}
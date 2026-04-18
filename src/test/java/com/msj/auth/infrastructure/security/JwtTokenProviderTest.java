package com.msj.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private static final Set<String> ROLES = Set.of("ROLE_USER");

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "test-secret-key-that-is-long-enough-for-hs512-minimum-512-bits-xxxxx");
        ReflectionTestUtils.setField(provider, "accessTokenExpirationMs", 900_000L);
        ReflectionTestUtils.setField(provider, "refreshTokenExpirationMs", 604_800_000L);
    }

    @Test
    void generateAccessToken_isValid() {
        String token = provider.generateAccessToken("jdoe", ROLES);

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("jdoe");
    }

    @Test
    void generateAccessToken_embedsRoles() {
        String token = provider.generateAccessToken("jdoe", Set.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(provider.getRolesFromToken(token)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void generateRefreshToken_isValid() {
        String token = provider.generateRefreshToken("jdoe", ROLES);

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("jdoe");
    }

    @Test
    void getRolesFromToken_returnsEmptySetWhenNoRolesClaim() {
        // refresh token without roles claim (legacy / no roles)
        assertThat(provider.getRolesFromToken(provider.generateRefreshToken("jdoe", Set.of()))).isEmpty();
    }

    @Test
    void validateToken_returnsFalseForGarbage() {
        assertThat(provider.validateToken("not.a.token")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(provider, "accessTokenExpirationMs", -1L);
        String expiredToken = provider.generateAccessToken("jdoe", ROLES);

        assertThat(provider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void accessAndRefreshTokens_areDifferent() {
        String access = provider.generateAccessToken("jdoe", ROLES);
        String refresh = provider.generateRefreshToken("jdoe", ROLES);

        assertThat(access).isNotEqualTo(refresh);
    }
}
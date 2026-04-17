package com.msj.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

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
        String token = provider.generateAccessToken("jdoe");

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("jdoe");
    }

    @Test
    void generateRefreshToken_isValid() {
        String token = provider.generateRefreshToken("jdoe");

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("jdoe");
    }

    @Test
    void validateToken_returnsFalseForGarbage() {
        assertThat(provider.validateToken("not.a.token")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(provider, "accessTokenExpirationMs", -1L);
        String expiredToken = provider.generateAccessToken("jdoe");

        assertThat(provider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void accessAndRefreshTokens_areDifferent() {
        String access = provider.generateAccessToken("jdoe");
        String refresh = provider.generateRefreshToken("jdoe");

        assertThat(access).isNotEqualTo(refresh);
    }
}
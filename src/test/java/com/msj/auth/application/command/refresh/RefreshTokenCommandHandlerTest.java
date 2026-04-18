package com.msj.auth.application.command.refresh;

import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import com.msj.auth.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCommandHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCommandHandler handler;

    @Test
    void handle_validToken_returnsNewAccessToken() {
        when(jwtTokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(refreshTokenRepository.isValid(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh")).thenReturn("jdoe");
        when(jwtTokenProvider.getRolesFromToken("valid-refresh")).thenReturn(Set.of("ROLE_USER"));
        when(jwtTokenProvider.generateAccessToken(eq("jdoe"), any())).thenReturn("new-access-token");

        String result = handler.handle(new RefreshTokenCommand("valid-refresh"));

        assertThat(result).isEqualTo("new-access-token");
    }

    @Test
    void handle_invalidJwt_throwsBadCredentials() {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand("bad-token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("refresh token");
    }

    @Test
    void handle_revokedToken_throwsBadCredentials() {
        when(jwtTokenProvider.validateToken("revoked-token")).thenReturn(true);
        when(refreshTokenRepository.isValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand("revoked-token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("refresh token");
    }
}

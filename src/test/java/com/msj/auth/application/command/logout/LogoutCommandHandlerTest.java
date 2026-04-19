package com.msj.auth.application.command.logout;

import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import com.msj.auth.infrastructure.security.JwtCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutCommandHandlerTest {

    @Mock
    private JwtCookieService cookieService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutCommandHandler handler;

    @Test
    void handle_revokesTokenAndClearsCookies() {
        HttpServletResponse response = new MockHttpServletResponse();

        handler.handle("jdoe", "raw-refresh-token", response);

        verify(refreshTokenRepository).revoke(anyString());
        verify(cookieService).clearAuthCookies(response);
    }

    @Test
    void handle_clearsCookiesEvenWhenRefreshTokenIsNull() {
        HttpServletResponse response = new MockHttpServletResponse();

        handler.handle("jdoe", null, response);

        verify(refreshTokenRepository, never()).revoke(anyString());
        verify(cookieService).clearAuthCookies(response);
    }
}
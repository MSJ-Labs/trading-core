package com.msj.auth.application.command.logout;

import com.msj.auth.infrastructure.security.JwtCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutCommandHandlerTest {

    @Mock
    private JwtCookieService cookieService;

    @InjectMocks
    private LogoutCommandHandler handler;

    @Test
    void handle_clearsCookiesForUser() {
        HttpServletResponse response = new MockHttpServletResponse();

        handler.handle("jdoe", response);

        verify(cookieService).clearAuthCookies(response);
    }
}
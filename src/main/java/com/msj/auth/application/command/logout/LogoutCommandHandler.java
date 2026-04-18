package com.msj.auth.application.command.logout;

import com.msj.auth.infrastructure.security.JwtCookieService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommandHandler {

    private final JwtCookieService cookieService;

    public void handle(String username, HttpServletResponse response) {
        log.info("Logging out user: {}", username);
        cookieService.clearAuthCookies(response);
    }
}
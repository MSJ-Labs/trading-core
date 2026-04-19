package com.msj.auth.application.command.logout;

import com.msj.auth.domain.token.TokenHasher;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    public void handle(String username, String rawRefreshToken, HttpServletResponse response) {
        log.info("Logging out user: {}", username);
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.revoke(TokenHasher.hash(rawRefreshToken));
        }
        cookieService.clearAuthCookies(response);
    }
}
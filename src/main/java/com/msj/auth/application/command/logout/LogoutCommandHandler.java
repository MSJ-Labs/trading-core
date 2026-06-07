package com.msj.auth.application.command.logout;

import com.msj.auth.domain.token.TokenHasher;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommandHandler {

    private final RefreshTokenRepository refreshTokenRepository;

    public void handle(LogoutCommand command) {
        log.info("Logging out user: {}", command.username());
        if (command.rawRefreshToken() != null && !command.rawRefreshToken().isBlank()) {
            refreshTokenRepository.revoke(TokenHasher.hash(command.rawRefreshToken()));
        }
    }
}
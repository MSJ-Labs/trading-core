package com.msj.auth.application.command.logout;

import com.msj.auth.domain.token.TokenHasher;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommandHandler {

    private final RefreshTokenRepository refreshTokenRepository;

    public void handle(LogoutCommand command) {
        log.info("Logging out user: {}", command.username());
        if (StringUtils.hasLength(command.rawRefreshToken())) {
            refreshTokenRepository.revoke(TokenHasher.hash(command.rawRefreshToken()));
        }
    }
}
package com.msj.auth.application.command.refresh;

import com.msj.auth.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCommandHandler {

    private final JwtTokenProvider jwtTokenProvider;

    public String handle(RefreshTokenCommand command) {
        String token = command.refreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        log.info("Refreshing access token for user: {}", username);
        return jwtTokenProvider.generateAccessToken(username);
    }
}
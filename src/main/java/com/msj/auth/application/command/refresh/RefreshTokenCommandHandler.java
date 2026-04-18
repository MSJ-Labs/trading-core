package com.msj.auth.application.command.refresh;

import com.msj.auth.domain.token.TokenHasher;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Two-layer validation:
     *   1. JWT signature + expiry  — stateless, catches forged/expired tokens
     *   2. DB revocation check     — stateful, catches tokens that are technically valid but
     *      were explicitly invalidated (logout, password change, stolen token revocation).
     *      We store and compare SHA-256 hashes, never the raw token.
     */
    public String handle(RefreshTokenCommand command) {
        String token = command.refreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        if (!refreshTokenRepository.isValid(TokenHasher.hash(token))) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        log.info("Refreshing access token for user: {}", username);
        return jwtTokenProvider.generateAccessToken(username, jwtTokenProvider.getRolesFromToken(token));
    }
}
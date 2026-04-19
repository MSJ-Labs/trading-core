package com.msj.auth.infrastructure.ports;

import com.msj.auth.domain.user.UserId;

import java.time.LocalDateTime;

public interface RefreshTokenRepository {

    void save(String tokenHash, UserId userId, LocalDateTime expiresAt);

    boolean isValid(String tokenHash);

    void revoke(String tokenHash);

    void revokeAllByUserId(UserId userId);
}

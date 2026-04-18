package com.msj.auth.infrastructure.adapters.persistence;

import com.msj.auth.domain.user.UserId;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.msj.infrastructure.jooq.Tables.REFRESH_TOKENS;

@Component
@RequiredArgsConstructor
public class JooqRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final DSLContext dsl;

    @Override
    @Transactional
    public void save(String tokenHash, UserId userId, LocalDateTime expiresAt) {
        dsl.insertInto(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.TOKEN_HASH, tokenHash)
                .set(REFRESH_TOKENS.USER_ID, userId.value().toLong())
                .set(REFRESH_TOKENS.EXPIRES_AT, expiresAt)
                .set(REFRESH_TOKENS.REVOKED, false)
                .set(REFRESH_TOKENS.CREATED_AT, LocalDateTime.now())
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValid(String tokenHash) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(REFRESH_TOKENS)
                        .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash))
                        .and(REFRESH_TOKENS.REVOKED.eq(false))
                        .and(REFRESH_TOKENS.EXPIRES_AT.gt(LocalDateTime.now()))
        );
    }

    @Override
    @Transactional
    public void revoke(String tokenHash) {
        dsl.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.REVOKED, true)
                .set(REFRESH_TOKENS.REVOKED_AT, LocalDateTime.now())
                .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash))
                .execute();
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UserId userId) {
        dsl.update(REFRESH_TOKENS)
                .set(REFRESH_TOKENS.REVOKED, true)
                .set(REFRESH_TOKENS.REVOKED_AT, LocalDateTime.now())
                .where(REFRESH_TOKENS.USER_ID.eq(userId.value().toLong()))
                .and(REFRESH_TOKENS.REVOKED.eq(false))
                .execute();
    }
}

-- Layer 1: server-side refresh token revocation (logout-all-devices, password change)
CREATE TABLE auth.refresh_tokens (
    token_hash   VARCHAR(64)  PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON auth.refresh_tokens(expires_at);

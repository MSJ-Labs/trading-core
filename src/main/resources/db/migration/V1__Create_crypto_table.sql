-- V1__Create_crypto_table.sql
-- Initial database schema for crypto assets

CREATE TABLE IF NOT EXISTS crypto (
    id BIGINT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    current_price NUMERIC(20, 8) NOT NULL,
    market_cap NUMERIC(30, 8),
    volume_24h NUMERIC(30, 8),
    change_percent_24h NUMERIC(10, 4),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for common queries
CREATE INDEX idx_crypto_symbol ON crypto(symbol);
CREATE INDEX idx_crypto_created_at ON crypto(created_at);
CREATE INDEX idx_crypto_updated_at ON crypto(updated_at);

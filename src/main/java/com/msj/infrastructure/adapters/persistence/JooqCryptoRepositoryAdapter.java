package com.msj.infrastructure.adapters.persistence;

import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import com.msj.infrastructure.ports.crypto.CryptoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.msj.infrastructure.jooq.Tables.CRYPTO;

/**
 * JOOQ-based persistence adapter for Crypto repository
 * Using JOOQ DSL for type-safe queries and proper transaction management
 * Following SOLID: Single Responsibility (crypto persistence only)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JooqCryptoRepositoryAdapter implements CryptoRepository {

    private final DSLContext dsl;

    @Override
    @Transactional
    public Crypto save(Crypto crypto) {
        log.debug("Saving crypto: {}", crypto.getId().value());

        // Upsert crypto
        dsl.insertInto(CRYPTO)
                .set(CRYPTO.ID, crypto.getId().value().toLong())
                .set(CRYPTO.SYMBOL, crypto.getSymbol())
                .set(CRYPTO.NAME, crypto.getName())
                .set(CRYPTO.CURRENT_PRICE, crypto.getCurrentPrice())
                .set(CRYPTO.MARKET_CAP, crypto.getMarketCap())
                .set(CRYPTO.VOLUME_24H, crypto.getVolume24h())
                .set(CRYPTO.CHANGE_PERCENT_24H, crypto.getChangePercent24h())
                .set(CRYPTO.DESCRIPTION, crypto.getDescription())
                .set(CRYPTO.CREATED_AT, crypto.getCreatedAt())
                .set(CRYPTO.UPDATED_AT, crypto.getUpdatedAt())
                .onConflict(CRYPTO.ID)
                .doUpdate()
                .set(CRYPTO.SYMBOL, crypto.getSymbol())
                .set(CRYPTO.NAME, crypto.getName())
                .set(CRYPTO.CURRENT_PRICE, crypto.getCurrentPrice())
                .set(CRYPTO.MARKET_CAP, crypto.getMarketCap())
                .set(CRYPTO.VOLUME_24H, crypto.getVolume24h())
                .set(CRYPTO.CHANGE_PERCENT_24H, crypto.getChangePercent24h())
                .set(CRYPTO.DESCRIPTION, crypto.getDescription())
                .set(CRYPTO.UPDATED_AT, crypto.getUpdatedAt())
                .execute();

        return crypto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Crypto> findById(CryptoId id) {
        log.debug("Finding crypto by id: {}", id.value());

        Record record = dsl.select()
                .from(CRYPTO)
                .where(CRYPTO.ID.eq(id.value().toLong()))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(mapToCrypto(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Crypto> findBySymbol(String symbol) {
        log.debug("Finding crypto by symbol: {}", symbol);

        Record record = dsl.select()
                .from(CRYPTO)
                .where(CRYPTO.SYMBOL.eq(symbol))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(mapToCrypto(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Crypto> findAll() {
        log.debug("Finding all cryptos");

        return dsl.select()
                .from(CRYPTO)
                .fetch()
                .map(this::mapToCrypto);
    }

    @Override
    @Transactional
    public void deleteById(CryptoId id) {
        log.debug("Deleting crypto by id: {}", id.value());

        dsl.deleteFrom(CRYPTO)
                .where(CRYPTO.ID.eq(id.value().toLong()))
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(CryptoId id) {
        log.debug("Checking if crypto exists by id: {}", id.value());

        return dsl.fetchExists(
            dsl.selectOne().from(CRYPTO).where(CRYPTO.ID.eq(id.value().toLong()))
        );
    }

    private Crypto mapToCrypto(Record record) {
        return new Crypto(
                CryptoId.of(record.get(CRYPTO.ID)),
                record.get(CRYPTO.SYMBOL),
                record.get(CRYPTO.NAME),
                record.get(CRYPTO.CURRENT_PRICE),
                record.get(CRYPTO.MARKET_CAP),
                record.get(CRYPTO.VOLUME_24H),
                record.get(CRYPTO.CHANGE_PERCENT_24H),
                record.get(CRYPTO.DESCRIPTION),
                record.get(CRYPTO.CREATED_AT),
                record.get(CRYPTO.UPDATED_AT)
        );
    }
}

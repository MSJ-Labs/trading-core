package com.msj.domain.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Unit tests for Crypto domain entity
 */
class CryptoTest {

    private static final String SYMBOL = "BTC";
    private static final String NAME = "Bitcoin";
    private static final BigDecimal PRICE = new BigDecimal("50000.00");
    private static final BigDecimal MARKET_CAP = new BigDecimal("1000000000000.00");
    private static final BigDecimal VOLUME_24H = new BigDecimal("50000000000.00");
    private static final BigDecimal CHANGE_PERCENT = new BigDecimal("2.50");
    private static final String DESCRIPTION = "The original cryptocurrency";

    private Crypto crypto;

    @BeforeEach
    void setUp() {
        crypto = Crypto.create(SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION);
    }

    @Test
    void testCreateCrypto() {
        assertSoftly(softly -> {
            softly.assertThat(crypto).isNotNull();
            softly.assertThat(crypto.getId()).isNotNull();
            softly.assertThat(crypto.getSymbol()).isEqualTo(SYMBOL);
            softly.assertThat(crypto.getName()).isEqualTo(NAME);
            softly.assertThat(crypto.getCurrentPrice()).isEqualTo(PRICE);
            softly.assertThat(crypto.getCreatedAt()).isNotNull();
            softly.assertThat(crypto.getUpdatedAt()).isNotNull();
        });
    }

    @Test
    void testCreateCryptoWithNullSymbolThrowsException() {
        assertThatThrownBy(() ->
                Crypto.create(null, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol");
    }

    @Test
    void testCreateCryptoWithBlankSymbolThrowsException() {
        assertThatThrownBy(() ->
                Crypto.create("", NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreateCryptoWithNullNameThrowsException() {
        assertThatThrownBy(() ->
                Crypto.create(SYMBOL, null, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdateCryptoName() {
        String newName = "Bitcoin Cash";
        crypto.update(newName, null, null, null, null, null);

        assertThat(crypto.getName()).isEqualTo(newName);
        assertThat(crypto.getCurrentPrice()).isEqualTo(PRICE);
        assertThat(crypto.getUpdatedAt()).isNotNull();
    }

    @Test
    void testUpdateCryptoPrice() {
        BigDecimal newPrice = new BigDecimal("55000.00");
        crypto.update(null, newPrice, null, null, null, null);

        assertThat(crypto.getName()).isEqualTo(NAME);
        assertThat(crypto.getCurrentPrice()).isEqualTo(newPrice);
    }

    @Test
    void testUpdateMultipleFields() {
        String newName = "Bitcoin Updated";
        BigDecimal newPrice = new BigDecimal("55000.00");
        BigDecimal newMarketCap = new BigDecimal("1100000000000.00");

        crypto.update(newName, newPrice, newMarketCap, null, null, "Updated description");

        assertSoftly(softly -> {
            softly.assertThat(crypto.getName()).isEqualTo(newName);
            softly.assertThat(crypto.getCurrentPrice()).isEqualTo(newPrice);
            softly.assertThat(crypto.getMarketCap()).isEqualTo(newMarketCap);
            softly.assertThat(crypto.getDescription()).isEqualTo("Updated description");
        });
    }

    @Test
    void testCryptoIdGeneration() {
        CryptoId id1 = CryptoId.generate();
        CryptoId id2 = CryptoId.generate();

        assertThat(id1.value()).isNotBlank();
        assertThat(id2.value()).isNotBlank();
        assertThat(id1.value()).isNotEqualTo(id2.value());
    }

    @Test
    void testCryptoIdOfMethod() {
        String value = "test-id";
        CryptoId id = CryptoId.of(value);

        assertThat(id.value()).isEqualTo(value);
    }

    @Test
    void testCryptoIdWithNullValueThrowsException() {
        assertThatThrownBy(() -> CryptoId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCryptoIdWithBlankValueThrowsException() {
        assertThatThrownBy(() -> CryptoId.of(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}


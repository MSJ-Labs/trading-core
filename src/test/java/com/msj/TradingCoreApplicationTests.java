package com.msj;

import com.msj.application.service.CryptoApplicationService;
import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for CryptoApplicationService with Spring Boot context
 */
@SpringBootTest
@ActiveProfiles("test")
class TradingCoreApplicationTests {

    @Autowired
    private CryptoApplicationService cryptoService;

    @Test
    void contextLoads() {
        assertNotNull(cryptoService);
    }

    @Test
    void testCreateAndRetrieveCrypto() {
        // Create a crypto
        Crypto created = cryptoService.createCrypto(
                "BTC",
                "Bitcoin",
                new BigDecimal("50000.00"),
                new BigDecimal("1000000000000.00"),
                new BigDecimal("50000000000.00"),
                new BigDecimal("2.50"),
                "The original cryptocurrency"
        );

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("BTC", created.getSymbol());
        assertEquals("Bitcoin", created.getName());

        // Retrieve the created crypto
        Crypto retrieved = cryptoService.getCryptoById(created.getId());
        assertNotNull(retrieved);
        assertEquals(created.getId().value(), retrieved.getId().value());
        assertEquals("BTC", retrieved.getSymbol());
    }

    @Test
    void testUpdateCrypto() {
        // Create
        Crypto created = cryptoService.createCrypto(
                "ETH",
                "Ethereum",
                new BigDecimal("3000.00"),
                null,
                null,
                null,
                "Smart contract platform"
        );

        // Update
        String newName = "Ethereum Updated";
        BigDecimal newPrice = new BigDecimal("3500.00");
        Crypto updated = cryptoService.updateCrypto(
                created.getId(),
                newName,
                newPrice,
                null,
                null,
                null,
                null
        );

        assertNotNull(updated);
        assertEquals(newName, updated.getName());
        assertEquals(newPrice, updated.getCurrentPrice());
    }

    @Test
    void testGetAllCryptos() {
        // Create multiple cryptos
        cryptoService.createCrypto("BTC", "Bitcoin", new BigDecimal("50000"), null, null, null, "Bitcoin");
        cryptoService.createCrypto("ETH", "Ethereum", new BigDecimal("3000"), null, null, null, "Ethereum");

        var allCryptos = cryptoService.getAllCryptos();
        assertNotNull(allCryptos);
        assertTrue(allCryptos.size() >= 2);
    }
}


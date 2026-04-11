package com.msj.application.service;

import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import com.msj.domain.crypto.CryptoNotFoundException;
import com.msj.infrastructure.ports.crypto.CryptoEventPublisher;
import com.msj.infrastructure.ports.crypto.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CryptoApplicationService
 * Using Mockito JUnit 5 extension and AssertJ for fluent assertions
 */
@ExtendWith(MockitoExtension.class)
class CryptoApplicationServiceTest {

    @Mock
    private CryptoRepository cryptoRepository;

    @Mock
    private CryptoEventPublisher eventPublisher;

    @InjectMocks
    private CryptoApplicationService cryptoService;

    private static final String SYMBOL = "BTC";
    private static final String NAME = "Bitcoin";
    private static final BigDecimal PRICE = new BigDecimal("50000.00");
    private static final BigDecimal MARKET_CAP = new BigDecimal("1000000000000.00");
    private static final BigDecimal VOLUME_24H = new BigDecimal("50000000000.00");
    private static final BigDecimal CHANGE_PERCENT = new BigDecimal("2.50");
    private static final String DESCRIPTION = "The original cryptocurrency";

    private Crypto testCrypto;
    private CryptoId testCryptoId;

    @BeforeEach
    void setUp() {
        testCryptoId = CryptoId.generate();
        testCrypto = Crypto.create(SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION);
        testCrypto.setId(testCryptoId);
    }

    @Test
    void testCreateCrypto() {
        when(cryptoRepository.save(any(Crypto.class))).thenReturn(testCrypto);

        Crypto result = cryptoService.createCrypto(SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION);

        assertThat(result)
                .isNotNull()
                .satisfies(crypto -> {
                    assertThat(crypto.getSymbol()).isEqualTo(SYMBOL);
                    assertThat(crypto.getName()).isEqualTo(NAME);
                });

        verify(cryptoRepository, times(1)).save(any(Crypto.class));
        verify(eventPublisher, times(1)).publishCryptoCreated(any(Crypto.class));
    }

    @Test
    void testGetCryptoById() {
        when(cryptoRepository.findById(testCryptoId)).thenReturn(Optional.of(testCrypto));

        Crypto result = cryptoService.getCryptoById(testCryptoId);

        assertThat(result)
                .isNotNull()
                .extracting(Crypto::getId)
                .isEqualTo(testCrypto.getId());

        verify(cryptoRepository, times(1)).findById(testCryptoId);
    }

    @Test
    void testGetCryptoByIdNotFound() {
        when(cryptoRepository.findById(testCryptoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cryptoService.getCryptoById(testCryptoId))
                .isInstanceOf(CryptoNotFoundException.class)
                .hasMessageContaining("not found");

        verify(cryptoRepository, times(1)).findById(testCryptoId);
    }

    @Test
    void testGetCryptoBySymbol() {
        when(cryptoRepository.findBySymbol(SYMBOL)).thenReturn(Optional.of(testCrypto));

        Crypto result = cryptoService.getCryptoBySymbol(SYMBOL);

        assertThat(result)
                .isNotNull()
                .extracting(Crypto::getSymbol)
                .isEqualTo(SYMBOL);

        verify(cryptoRepository, times(1)).findBySymbol(SYMBOL);
    }

    @Test
    void testGetCryptoBySymbolNotFound() {
        when(cryptoRepository.findBySymbol(SYMBOL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cryptoService.getCryptoBySymbol(SYMBOL))
                .isInstanceOf(CryptoNotFoundException.class);

        verify(cryptoRepository, times(1)).findBySymbol(SYMBOL);
    }

    @Test
    void testGetAllCryptos() {
        Crypto crypto2 = Crypto.create("ETH", "Ethereum", new BigDecimal("3000"), null, null, null, "Smart contracts platform");
        List<Crypto> expectedCryptos = Arrays.asList(testCrypto, crypto2);
        when(cryptoRepository.findAll()).thenReturn(expectedCryptos);

        List<Crypto> result = cryptoService.getAllCryptos();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expectedCryptos);

        verify(cryptoRepository, times(1)).findAll();
    }

    @Test
    void testUpdateCrypto() {
        String newName = "Bitcoin Updated";
        BigDecimal newPrice = new BigDecimal("55000.00");

        when(cryptoRepository.findById(testCryptoId)).thenReturn(Optional.of(testCrypto));
        when(cryptoRepository.save(any(Crypto.class))).thenReturn(testCrypto);

        Crypto result = cryptoService.updateCrypto(testCryptoId, newName, newPrice, null, null, null, null);

        assertThat(result)
                .isNotNull()
                .satisfies(crypto -> {
                    assertThat(crypto.getName()).isEqualTo(newName);
                    assertThat(crypto.getCurrentPrice()).isEqualTo(newPrice);
                });

        verify(cryptoRepository, times(1)).findById(testCryptoId);
        verify(cryptoRepository, times(1)).save(any(Crypto.class));
        verify(eventPublisher, times(1)).publishCryptoUpdated(any(Crypto.class));
    }

    @Test
    void testUpdateCryptoNotFound() {
        when(cryptoRepository.findById(testCryptoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                cryptoService.updateCrypto(testCryptoId, "New Name", PRICE, null, null, null, null))
                .isInstanceOf(CryptoNotFoundException.class);

        verify(cryptoRepository, times(1)).findById(testCryptoId);
        verify(cryptoRepository, never()).save(any(Crypto.class));
    }

    @Test
    void testDeleteCrypto() {
        when(cryptoRepository.existsById(testCryptoId)).thenReturn(true);

        cryptoService.deleteCrypto(testCryptoId);

        verify(cryptoRepository, times(1)).existsById(testCryptoId);
        verify(cryptoRepository, times(1)).deleteById(testCryptoId);
        verify(eventPublisher, times(1)).publishCryptoDeleted(testCryptoId.value());
    }

    @Test
    void testDeleteCryptoNotFound() {
        when(cryptoRepository.existsById(testCryptoId)).thenReturn(false);

        assertThatThrownBy(() -> cryptoService.deleteCrypto(testCryptoId))
                .isInstanceOf(CryptoNotFoundException.class);

        verify(cryptoRepository, times(1)).existsById(testCryptoId);
        verify(cryptoRepository, never()).deleteById(testCryptoId);
    }
}


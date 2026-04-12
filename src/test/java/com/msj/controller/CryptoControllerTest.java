package com.msj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msj.application.service.CryptoApplicationService;
import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CryptoController REST API
 */
@WebMvcTest(CryptoController.class)
class CryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CryptoApplicationService cryptoService;

    private static final String API_URL = "/api/v1/cryptos";
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
        testCrypto = new Crypto(
                testCryptoId,
                SYMBOL,
                NAME,
                PRICE,
                MARKET_CAP,
                VOLUME_24H,
                CHANGE_PERCENT,
                DESCRIPTION,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void testCreateCrypto() throws Exception {
        when(cryptoService.createCrypto(SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION))
                .thenReturn(testCrypto);

        CryptoController.CreateCryptoRequest request = new CryptoController.CreateCryptoRequest(
                SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION
        );

        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.name").value(NAME));

        verify(cryptoService, times(1)).createCrypto(SYMBOL, NAME, PRICE, MARKET_CAP, VOLUME_24H, CHANGE_PERCENT, DESCRIPTION);
    }

    @Test
    void testGetCryptoById() throws Exception {
        when(cryptoService.getCryptoById(testCryptoId)).thenReturn(testCrypto);

        mockMvc.perform(get(API_URL + "/" + testCryptoId.value().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.name").value(NAME));

        verify(cryptoService, times(1)).getCryptoById(any(CryptoId.class));
    }

    @Test
    void testGetCryptoBySymbol() throws Exception {
        when(cryptoService.getCryptoBySymbol(SYMBOL)).thenReturn(testCrypto);

        mockMvc.perform(get(API_URL + "/symbol/" + SYMBOL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.name").value(NAME));

        verify(cryptoService, times(1)).getCryptoBySymbol(SYMBOL);
    }

    @Test
    void testGetAllCryptos() throws Exception {
        Crypto crypto2 = new Crypto(
                CryptoId.generate(),
                "ETH",
                "Ethereum",
                new BigDecimal("3000"),
                null,
                null,
                null,
                "Smart contracts platform",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<Crypto> cryptos = Arrays.asList(testCrypto, crypto2);
        when(cryptoService.getAllCryptos()).thenReturn(cryptos);

        mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].symbol").value(SYMBOL))
                .andExpect(jsonPath("$[1].symbol").value("ETH"));

        verify(cryptoService, times(1)).getAllCryptos();
    }

    @Test
    void testUpdateCrypto() throws Exception {
        String newName = "Bitcoin Updated";
        BigDecimal newPrice = new BigDecimal("55000.00");

        testCrypto.setName(newName);
        testCrypto.setCurrentPrice(newPrice);

        when(cryptoService.updateCrypto(eq(testCryptoId), eq(newName), eq(newPrice), any(), any(), any(), any()))
                .thenReturn(testCrypto);

        CryptoController.UpdateCryptoRequest request = new CryptoController.UpdateCryptoRequest(
                newName, newPrice, null, null, null, null
        );

        mockMvc.perform(put(API_URL + "/" + testCryptoId.value().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.currentPrice").value(newPrice.doubleValue()));

        verify(cryptoService, times(1)).updateCrypto(any(CryptoId.class), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDeleteCrypto() throws Exception {
        mockMvc.perform(delete(API_URL + "/" + testCryptoId.value().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cryptoService, times(1)).deleteCrypto(any(CryptoId.class));
    }
}

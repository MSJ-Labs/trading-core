package com.msj.controller;

import com.msj.application.service.CryptoApplicationService;
import com.msj.controller.dto.CreateCryptoRequest;
import com.msj.controller.dto.CryptoResponse;
import com.msj.controller.dto.UpdateCryptoRequest;
import com.msj.controller.mapper.CryptoMapper;
import com.msj.domain.crypto.Crypto;
import com.msj.domain.crypto.CryptoId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Crypto endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cryptos")
@RequiredArgsConstructor
@Tag(name = "Crypto", description = "Cryptocurrency management endpoints")
public class CryptoController {

    private final CryptoApplicationService cryptoService;
    private final CryptoMapper cryptoMapper;

    /**
     * Create a new crypto asset
     */
    @PostMapping
    @Operation(summary = "Create crypto", description = "Create a new cryptocurrency asset")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<CryptoResponse> createCrypto(@RequestBody CreateCryptoRequest request) {
        log.info("Creating crypto: {}", request.getSymbol());
        Crypto crypto = cryptoService.createCrypto(
                request.getSymbol(),
                request.getName(),
                request.getCurrentPrice(),
                request.getMarketCap(),
                request.getVolume24h(),
                request.getChangePercent24h(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(cryptoMapper.toResponse(crypto));
    }

    /**
     * Get crypto by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get crypto by ID", description = "Retrieve a cryptocurrency asset by its ID")
    public ResponseEntity<CryptoResponse> getCryptoById(@PathVariable String id) {
        log.info("Fetching crypto with id: {}", id);
        Crypto crypto = cryptoService.getCryptoById(CryptoId.of(id));
        return ResponseEntity.ok(cryptoMapper.toResponse(crypto));
    }

    /**
     * Get crypto by symbol
     */
    @GetMapping("/symbol/{symbol}")
    @Operation(summary = "Get crypto by symbol", description = "Retrieve a cryptocurrency asset by its symbol (e.g., BTC)")
    public ResponseEntity<CryptoResponse> getCryptoBySymbol(@PathVariable String symbol) {
        log.info("Fetching crypto with symbol: {}", symbol);
        Crypto crypto = cryptoService.getCryptoBySymbol(symbol);
        return ResponseEntity.ok(cryptoMapper.toResponse(crypto));
    }

    /**
     * Get all cryptos
     */
    @GetMapping
    @Operation(summary = "Get all cryptos", description = "Retrieve all cryptocurrency assets")
    public ResponseEntity<List<CryptoResponse>> getAllCryptos() {
        log.info("Fetching all cryptos");
        List<Crypto> cryptos = cryptoService.getAllCryptos();
        List<CryptoResponse> cryptoResponses = cryptos.stream()
                .map(cryptoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cryptoResponses);
    }

    /**
     * Update crypto
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update crypto", description = "Update an existing cryptocurrency asset")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<CryptoResponse> updateCrypto(@PathVariable String id, @RequestBody UpdateCryptoRequest request) {
        log.info("Updating crypto with id: {}", id);
        Crypto crypto = cryptoService.updateCrypto(
                CryptoId.of(id),
                request.getName(),
                request.getCurrentPrice(),
                request.getMarketCap(),
                request.getVolume24h(),
                request.getChangePercent24h(),
                request.getDescription()
        );
        return ResponseEntity.ok(cryptoMapper.toResponse(crypto));
    }

    /**
     * Delete crypto
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete crypto", description = "Delete a cryptocurrency asset")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCrypto(@PathVariable String id) {
        log.info("Deleting crypto with id: {}", id);
        cryptoService.deleteCrypto(CryptoId.of(id));
        return ResponseEntity.noContent().build();
    }
}

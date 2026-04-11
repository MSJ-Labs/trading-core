package com.msj.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for crypto asset response
 * Using Lombok Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoResponse {

    private String id;
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal changePercent24h;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

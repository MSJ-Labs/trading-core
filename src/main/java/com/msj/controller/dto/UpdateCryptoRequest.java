package com.msj.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating an existing crypto asset
 * Using Lombok Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCryptoRequest {

    private String name;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal changePercent24h;
    private String description;
}

package com.msj.marketdata.application.command;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceTickCommand(String symbol, BigDecimal price, Instant timestamp) {}
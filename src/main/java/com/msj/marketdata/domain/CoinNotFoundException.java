package com.msj.marketdata.domain;

public class CoinNotFoundException extends RuntimeException {

    public CoinNotFoundException(String coinId) {
        super("Coin not found: " + coinId);
    }
}
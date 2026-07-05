package com.msj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot application entry point
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class TradingCoreApplication {

    static void main(String[] args) {
        SpringApplication.run(TradingCoreApplication.class, args);
    }
}


package com.msj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class AsyncConfig {

    // Spring looks for a bean named "taskExecutor" for @Async. Without it, the several
    // executors registered by the STOMP broker make it fall back to a default executor.
    @Bean(name = "taskExecutor")
    SimpleAsyncTaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("async-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
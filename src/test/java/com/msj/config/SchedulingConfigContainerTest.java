package com.msj.config;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Real Redis (Docker): only one replica gets the refresh lock per window. */
@Testcontainers
class SchedulingConfigContainerTest {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Testcontainers
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static LockProvider lockProvider;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(REDIS.getHost(), REDIS.getMappedPort(6379)));
        connectionFactory.afterPropertiesSet();
        lockProvider = new SchedulingConfig().lockProvider(connectionFactory);
    }

    @AfterAll
    static void disconnect() {
        connectionFactory.destroy();
    }

    @Test
    void second_replica_cannot_take_the_lock_while_first_holds_it() {
        LockConfiguration config = config("held");

        Optional<SimpleLock> first = lockProvider.lock(config);
        Optional<SimpleLock> second = lockProvider.lock(config);

        assertThat(first).isPresent();
        assertThat(second).isEmpty();
    }

    @Test
    void lock_stays_taken_after_unlock_until_lockAtLeastFor_elapsed() {
        LockConfiguration config = config("at-least");

        lockProvider.lock(config).orElseThrow().unlock();

        assertThat(lockProvider.lock(config)).isEmpty();
    }

    private static LockConfiguration config(String name) {
        return new LockConfiguration(Instant.now(), name, Duration.ofSeconds(55), Duration.ofSeconds(30));
    }
}
package com.msj.auth.infrastructure.adapters.persistence;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserId;
import com.msj.auth.infrastructure.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.msj.infrastructure.jooq.Tables.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUserRepositoryAdapter implements UserRepository {

    private final DSLContext dsl;

    // Default role ID for ROLE_USER (seeded in V2 migration)
    private static final long DEFAULT_ROLE_ID = 2L;

    @Override
    @Transactional
    public User save(User user) {
        long id = user.getId().value().toLong();

        dsl.insertInto(USERS)
                .set(USERS.ID, id)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD, user.getPasswordHash())
                .set(USERS.FIRST_NAME, user.getFirstName())
                .set(USERS.LAST_NAME, user.getLastName())
                .set(USERS.ENABLED, user.isEnabled())
                .set(USERS.ACCOUNT_NON_EXPIRED, user.isAccountNonExpired())
                .set(USERS.ACCOUNT_NON_LOCKED, user.isAccountNonLocked())
                .set(USERS.CREDENTIALS_NON_EXPIRED, user.isCredentialsNonExpired())
                .set(USERS.CREATED_AT, user.getCreatedAt())
                .set(USERS.UPDATED_AT, user.getUpdatedAt())
                .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
                .set(USERS.FAILED_LOGIN_ATTEMPTS, user.getFailedLoginAttempts())
                .set(USERS.LOCKED_UNTIL, user.getLockedUntil())
                .onConflict(USERS.ID)
                .doUpdate()
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD, user.getPasswordHash())
                .set(USERS.FIRST_NAME, user.getFirstName())
                .set(USERS.LAST_NAME, user.getLastName())
                .set(USERS.ENABLED, user.isEnabled())
                .set(USERS.ACCOUNT_NON_EXPIRED, user.isAccountNonExpired())
                .set(USERS.ACCOUNT_NON_LOCKED, user.isAccountNonLocked())
                .set(USERS.CREDENTIALS_NON_EXPIRED, user.isCredentialsNonExpired())
                .set(USERS.UPDATED_AT, user.getUpdatedAt())
                .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
                .set(USERS.FAILED_LOGIN_ATTEMPTS, user.getFailedLoginAttempts())
                .set(USERS.LOCKED_UNTIL, user.getLockedUntil())
                .execute();

        // Assign default ROLE_USER on first save (no-op if already exists)
        dsl.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_ID, id)
                .set(USER_ROLES.ROLE_ID, DEFAULT_ROLE_ID)
                .set(USER_ROLES.ASSIGNED_AT, LocalDateTime.now())
                .onConflict(USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                .doNothing()
                .execute();

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        Record record = dsl.select()
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne();

        return Optional.ofNullable(record).map(r -> mapWithRoles(r, UserId.of(r.get(USERS.ID))));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Record record = dsl.select()
                .from(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOne();

        return Optional.ofNullable(record).map(r -> mapWithRoles(r, UserId.of(r.get(USERS.ID))));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        Record record = dsl.select()
                .from(USERS)
                .where(USERS.ID.eq(id.value().toLong()))
                .fetchOne();

        return Optional.ofNullable(record).map(r -> mapWithRoles(r, id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.USERNAME.eq(username)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.EMAIL.eq(email)));
    }

    // Single query — user + roles joined, no N+1
    private User mapWithRoles(Record record, UserId userId) {
        Set<String> roles = dsl.select(ROLES.NAME)
                .from(USER_ROLES)
                .join(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                .where(USER_ROLES.USER_ID.eq(userId.value().toLong()))
                .fetchSet(ROLES.NAME);

        return User.builder()
                .id(userId)
                .username(record.get(USERS.USERNAME))
                .email(record.get(USERS.EMAIL))
                .passwordHash(record.get(USERS.PASSWORD))
                .firstName(record.get(USERS.FIRST_NAME))
                .lastName(record.get(USERS.LAST_NAME))
                .enabled(record.get(USERS.ENABLED))
                .accountNonExpired(record.get(USERS.ACCOUNT_NON_EXPIRED))
                .accountNonLocked(record.get(USERS.ACCOUNT_NON_LOCKED))
                .credentialsNonExpired(record.get(USERS.CREDENTIALS_NON_EXPIRED))
                .createdAt(record.get(USERS.CREATED_AT, LocalDateTime.class))
                .updatedAt(record.get(USERS.UPDATED_AT, LocalDateTime.class))
                .lastLoginAt(record.get(USERS.LAST_LOGIN_AT, LocalDateTime.class))
                .failedLoginAttempts(record.get(USERS.FAILED_LOGIN_ATTEMPTS) != null
                        ? record.get(USERS.FAILED_LOGIN_ATTEMPTS) : 0)
                .lockedUntil(record.get(USERS.LOCKED_UNTIL, LocalDateTime.class))
                .roles(roles)
                .build();
    }
}
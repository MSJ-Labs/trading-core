package com.msj.infrastructure.adapters.persistence;

import com.msj.domain.user.*;
import com.msj.infrastructure.ports.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.msj.infrastructure.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

/**
 * JOOQ-based persistence adapter for User repository
 * Using JOOQ DSL for type-safe queries and better maintainability
 * Following SOLID: Single Responsibility (user persistence only)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUserRepositoryAdapter implements UserRepository {

    private final DSLContext dsl;

    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getId().value());

        // Upsert user
        dsl.insertInto(USERS)
                .set(USERS.ID, user.getId().value().toLong())
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD, user.getPassword())
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
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD, user.getPassword())
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

        // Save profile if exists
        if (user.getProfile() != null) {
            saveUserProfile(user.getProfile());
        }

        // Save user roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            saveUserRoles(user.getId(), user.getRoles());
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        log.debug("Finding user by id: {}", id.value());

        Record record = dsl.select()
                .from(USERS.leftJoin(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.USER_ID)))
                .where(USERS.ID.eq(id.value().toLong()))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        User user = mapToUser(record);
        user.setRoles(loadUserRoles(user.getId()));
        return Optional.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);

        Record record = dsl.select()
                .from(USERS.leftJoin(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.USER_ID)))
                .where(USERS.USERNAME.eq(username))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        User user = mapToUser(record);
        user.setRoles(loadUserRoles(user.getId()));
        return Optional.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        Record record = dsl.select()
                .from(USERS.leftJoin(USER_PROFILES).on(USERS.ID.eq(USER_PROFILES.USER_ID)))
                .where(USERS.EMAIL.eq(email))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        User user = mapToUser(record);
        user.setRoles(loadUserRoles(user.getId()));
        return Optional.of(user);
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

    @Override
    @Transactional
    public void deleteById(UserId id) {
        log.debug("Deleting user by id: {}", id.value());

        // Delete user roles first (cascade will handle this, but explicit is better)
        dsl.deleteFrom(USER_ROLES).where(USER_ROLES.USER_ID.eq(id.value().toLong())).execute();

        // Delete user profile
        dsl.deleteFrom(USER_PROFILES).where(USER_PROFILES.USER_ID.eq(id.value().toLong())).execute();

        // Delete user
        dsl.deleteFrom(USERS).where(USERS.ID.eq(id.value().toLong())).execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UserId id) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.ID.eq(id.value().toLong())));
    }

    private User mapToUser(Record record) {
        User user = User.builder()
                .id(UserId.of(record.get(USERS.ID).longValue()))
                .username(record.get(USERS.USERNAME))
                .email(record.get(USERS.EMAIL))
                .password(record.get(USERS.PASSWORD))
                .firstName(record.get(USERS.FIRST_NAME))
                .lastName(record.get(USERS.LAST_NAME))
                .enabled(record.get(USERS.ENABLED))
                .accountNonExpired(record.get(USERS.ACCOUNT_NON_EXPIRED))
                .accountNonLocked(record.get(USERS.ACCOUNT_NON_LOCKED))
                .credentialsNonExpired(record.get(USERS.CREDENTIALS_NON_EXPIRED))
                .createdAt(record.get(USERS.CREATED_AT))
                .updatedAt(record.get(USERS.UPDATED_AT))
                .lastLoginAt(record.get(USERS.LAST_LOGIN_AT))
                .failedLoginAttempts(record.get(USERS.FAILED_LOGIN_ATTEMPTS))
                .lockedUntil(record.get(USERS.LOCKED_UNTIL))
                .build();

        // Map profile if exists
        if (record.get(USER_PROFILES.ID) != null) {
            UserProfile profile = UserProfile.builder()
                    .id(UserProfileId.of(record.get(USER_PROFILES.ID).longValue()))
                    .userId(user.getId())
                    .timezone(record.get(USER_PROFILES.TIMEZONE))
                    .language(record.get(USER_PROFILES.LANGUAGE))
                    .theme(record.get(USER_PROFILES.THEME))
                    .notificationsEnabled(record.get(USER_PROFILES.NOTIFICATIONS_ENABLED))
                    .twoFactorEnabled(record.get(USER_PROFILES.TWO_FACTOR_ENABLED))
                    .createdAt(record.get(USER_PROFILES.CREATED_AT))
                    .updatedAt(record.get(USER_PROFILES.UPDATED_AT))
                    .build();
            user.setProfile(profile);
        }

        return user;
    }

    private Set<Role> loadUserRoles(UserId userId) {
        Set<Role> roles = new HashSet<>();

        Result<Record> records = dsl.select()
                .from(ROLES)
                .join(USER_ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                .where(USER_ROLES.USER_ID.eq(userId.value().toLong()))
                .fetch();

        for (Record record : records) {
            Role role = Role.builder()
                    .id(RoleId.of(record.get(ROLES.ID).longValue()))
                    .name(record.get(ROLES.NAME))
                    .description(record.get(ROLES.DESCRIPTION))
                    .createdAt(record.get(ROLES.CREATED_AT))
                    .permissions(loadRolePermissions(RoleId.of(record.get(ROLES.ID).longValue())))
                    .build();
            roles.add(role);
        }

        return roles;
    }

    private Set<Permission> loadRolePermissions(RoleId roleId) {
        Set<Permission> permissions = new HashSet<>();

        Result<Record> records = dsl.select()
                .from(PERMISSIONS)
                .join(ROLE_PERMISSIONS).on(PERMISSIONS.ID.eq(ROLE_PERMISSIONS.PERMISSION_ID))
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(roleId.value().toLong()))
                .fetch();

        for (Record record : records) {
            Permission permission = Permission.builder()
                    .id(PermissionId.of(record.get(PERMISSIONS.ID).longValue()))
                    .name(record.get(PERMISSIONS.NAME))
                    .description(record.get(PERMISSIONS.DESCRIPTION))
                    .resource(record.get(PERMISSIONS.RESOURCE))
                    .action(record.get(PERMISSIONS.ACTION))
                    .createdAt(record.get(PERMISSIONS.CREATED_AT))
                    .build();
            permissions.add(permission);
        }

        return permissions;
    }

    private void saveUserProfile(UserProfile profile) {
        dsl.insertInto(USER_PROFILES)
                .set(USER_PROFILES.ID, profile.getId().value().toLong())
                .set(USER_PROFILES.USER_ID, profile.getUserId().value().toLong())
                .set(USER_PROFILES.TIMEZONE, profile.getTimezone())
                .set(USER_PROFILES.LANGUAGE, profile.getLanguage())
                .set(USER_PROFILES.THEME, profile.getTheme())
                .set(USER_PROFILES.NOTIFICATIONS_ENABLED, profile.isNotificationsEnabled())
                .set(USER_PROFILES.TWO_FACTOR_ENABLED, profile.isTwoFactorEnabled())
                .set(USER_PROFILES.CREATED_AT, profile.getCreatedAt())
                .set(USER_PROFILES.UPDATED_AT, profile.getUpdatedAt())
                .onConflict(USER_PROFILES.USER_ID)
                .doUpdate()
                .set(USER_PROFILES.TIMEZONE, profile.getTimezone())
                .set(USER_PROFILES.LANGUAGE, profile.getLanguage())
                .set(USER_PROFILES.THEME, profile.getTheme())
                .set(USER_PROFILES.NOTIFICATIONS_ENABLED, profile.isNotificationsEnabled())
                .set(USER_PROFILES.TWO_FACTOR_ENABLED, profile.isTwoFactorEnabled())
                .set(USER_PROFILES.UPDATED_AT, profile.getUpdatedAt())
                .execute();
    }

    private void saveUserRoles(UserId userId, Set<Role> roles) {
        // First, remove existing roles
        dsl.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(userId.value().toLong()))
                .execute();

        // Then, insert new roles
        for (Role role : roles) {
            dsl.insertInto(USER_ROLES)
                    .set(USER_ROLES.USER_ID, userId.value().toLong())
                    .set(USER_ROLES.ROLE_ID, role.getId().value().toLong())
                    .set(USER_ROLES.ASSIGNED_AT, LocalDateTime.now())
                    .execute();
        }
    }
}

package com.msj.infrastructure.jooq;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JOOQ table definitions for database schema
 * Using modern JOOQ approach with TSID (BIGINT) for all IDs
 * Following JOOQ best practices and professional standards
 */
public class Tables {

    public static final Users USERS = new Users();
    public static final Roles ROLES = new Roles();
    public static final Permissions PERMISSIONS = new Permissions();
    public static final UserRoles USER_ROLES = new UserRoles();
    public static final RolePermissions ROLE_PERMISSIONS = new RolePermissions();
    public static final UserProfiles USER_PROFILES = new UserProfiles();
    public static final Crypto CRYPTO = new Crypto();

    /**
     * Users table definition
     */
    public static class Users extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> USERNAME = DSL.field(DSL.name("username"), org.jooq.impl.SQLDataType.VARCHAR(100).notNull());
        public final Field<String> EMAIL = DSL.field(DSL.name("email"), org.jooq.impl.SQLDataType.VARCHAR(255).notNull());
        public final Field<String> PASSWORD = DSL.field(DSL.name("password"), org.jooq.impl.SQLDataType.VARCHAR(255).notNull());
        public final Field<String> FIRST_NAME = DSL.field(DSL.name("first_name"), org.jooq.impl.SQLDataType.VARCHAR(100));
        public final Field<String> LAST_NAME = DSL.field(DSL.name("last_name"), org.jooq.impl.SQLDataType.VARCHAR(100));
        public final Field<Boolean> ENABLED = DSL.field(DSL.name("enabled"), org.jooq.impl.SQLDataType.BOOLEAN.notNull());
        public final Field<Boolean> ACCOUNT_NON_EXPIRED = DSL.field(DSL.name("account_non_expired"), org.jooq.impl.SQLDataType.BOOLEAN.notNull());
        public final Field<Boolean> ACCOUNT_NON_LOCKED = DSL.field(DSL.name("account_non_locked"), org.jooq.impl.SQLDataType.BOOLEAN.notNull());
        public final Field<Boolean> CREDENTIALS_NON_EXPIRED = DSL.field(DSL.name("credentials_non_expired"), org.jooq.impl.SQLDataType.BOOLEAN.notNull());
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());
        public final Field<LocalDateTime> UPDATED_AT = DSL.field(DSL.name("updated_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());
        public final Field<LocalDateTime> LAST_LOGIN_AT = DSL.field(DSL.name("last_login_at"), org.jooq.impl.SQLDataType.LOCALDATETIME);
        public final Field<Integer> FAILED_LOGIN_ATTEMPTS = DSL.field(DSL.name("failed_login_attempts"), org.jooq.impl.SQLDataType.INTEGER);
        public final Field<LocalDateTime> LOCKED_UNTIL = DSL.field(DSL.name("locked_until"), org.jooq.impl.SQLDataType.LOCALDATETIME);

        public Users() {
            super(DSL.name("users"));
        }
    }

    /**
     * Roles table definition
     */
    public static class Roles extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> NAME = DSL.field(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(50).notNull());
        public final Field<String> DESCRIPTION = DSL.field(DSL.name("description"), org.jooq.impl.SQLDataType.VARCHAR(255));
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public Roles() {
            super(DSL.name("roles"));
        }
    }

    /**
     * Permissions table definition
     */
    public static class Permissions extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> NAME = DSL.field(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(100).notNull());
        public final Field<String> DESCRIPTION = DSL.field(DSL.name("description"), org.jooq.impl.SQLDataType.VARCHAR(255));
        public final Field<String> RESOURCE = DSL.field(DSL.name("resource"), org.jooq.impl.SQLDataType.VARCHAR(100).notNull());
        public final Field<String> ACTION = DSL.field(DSL.name("action"), org.jooq.impl.SQLDataType.VARCHAR(50).notNull());
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public Permissions() {
            super(DSL.name("permissions"));
        }
    }

    /**
     * User-Role junction table definition
     */
    public static class UserRoles extends TableImpl<org.jooq.Record> {
        public final Field<Long> USER_ID = DSL.field(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<Long> ROLE_ID = DSL.field(DSL.name("role_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<LocalDateTime> ASSIGNED_AT = DSL.field(DSL.name("assigned_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public UserRoles() {
            super(DSL.name("user_roles"));
        }
    }

    /**
     * Role-Permission junction table definition
     */
    public static class RolePermissions extends TableImpl<org.jooq.Record> {
        public final Field<Long> ROLE_ID = DSL.field(DSL.name("role_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<Long> PERMISSION_ID = DSL.field(DSL.name("permission_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<LocalDateTime> ASSIGNED_AT_TIME = DSL.field(DSL.name("assigned_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public RolePermissions() {
            super(DSL.name("role_permissions"));
        }
    }

    /**
     * User profiles table definition
     */
    public static class UserProfiles extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<Long> USER_ID = DSL.field(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> TIMEZONE = DSL.field(DSL.name("timezone"), org.jooq.impl.SQLDataType.VARCHAR(50));
        public final Field<String> LANGUAGE = DSL.field(DSL.name("language"), org.jooq.impl.SQLDataType.VARCHAR(10));
        public final Field<String> THEME = DSL.field(DSL.name("theme"), org.jooq.impl.SQLDataType.VARCHAR(20));
        public final Field<Boolean> NOTIFICATIONS_ENABLED = DSL.field(DSL.name("notifications_enabled"), org.jooq.impl.SQLDataType.BOOLEAN);
        public final Field<Boolean> TWO_FACTOR_ENABLED = DSL.field(DSL.name("two_factor_enabled"), org.jooq.impl.SQLDataType.BOOLEAN);
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());
        public final Field<LocalDateTime> UPDATED_AT = DSL.field(DSL.name("updated_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public UserProfiles() {
            super(DSL.name("user_profiles"));
        }
    }

    /**
     * Crypto assets table definition
     */
    public static class Crypto extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> SYMBOL = DSL.field(DSL.name("symbol"), org.jooq.impl.SQLDataType.VARCHAR(10).notNull());
        public final Field<String> NAME = DSL.field(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(255).notNull());
        public final Field<BigDecimal> CURRENT_PRICE = DSL.field(DSL.name("current_price"), org.jooq.impl.SQLDataType.DECIMAL(20, 8).notNull());
        public final Field<BigDecimal> MARKET_CAP = DSL.field(DSL.name("market_cap"), org.jooq.impl.SQLDataType.DECIMAL(30, 8));
        public final Field<BigDecimal> VOLUME_24H = DSL.field(DSL.name("volume_24h"), org.jooq.impl.SQLDataType.DECIMAL(30, 8));
        public final Field<BigDecimal> CHANGE_PERCENT_24H = DSL.field(DSL.name("change_percent_24h"), org.jooq.impl.SQLDataType.DECIMAL(10, 4));
        public final Field<String> DESCRIPTION = DSL.field(DSL.name("description"), org.jooq.impl.SQLDataType.VARCHAR(1000));
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());
        public final Field<LocalDateTime> UPDATED_AT = DSL.field(DSL.name("updated_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public Crypto() {
            super(DSL.name("crypto"));
        }
    }
}

package com.msj.auth.infrastructure.adapters.persistence;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.time.LocalDateTime;

@UtilityClass
public class Tables {

    public static final Users USERS = new Users();
    public static final Roles ROLES = new Roles();
    public static final UserRoles USER_ROLES = new UserRoles();
    public static final RefreshTokens REFRESH_TOKENS = new RefreshTokens();

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
            super(DSL.name("auth", "users"));
        }
    }

    public static class Roles extends TableImpl<org.jooq.Record> {
        public final Field<Long> ID = DSL.field(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<String> NAME = DSL.field(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(50).notNull());
        public final Field<String> DESCRIPTION = DSL.field(DSL.name("description"), org.jooq.impl.SQLDataType.VARCHAR(255));
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public Roles() {
            super(DSL.name("auth", "roles"));
        }
    }

    public static class UserRoles extends TableImpl<org.jooq.Record> {
        public final Field<Long> USER_ID = DSL.field(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<Long> ROLE_ID = DSL.field(DSL.name("role_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<LocalDateTime> ASSIGNED_AT = DSL.field(DSL.name("assigned_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public UserRoles() {
            super(DSL.name("auth", "user_roles"));
        }
    }

    public static class RefreshTokens extends TableImpl<org.jooq.Record> {
        public final Field<String> TOKEN_HASH = DSL.field(DSL.name("token_hash"), org.jooq.impl.SQLDataType.VARCHAR(64).notNull());
        public final Field<Long> USER_ID = DSL.field(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.notNull());
        public final Field<LocalDateTime> EXPIRES_AT = DSL.field(DSL.name("expires_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());
        public final Field<Boolean> REVOKED = DSL.field(DSL.name("revoked"), org.jooq.impl.SQLDataType.BOOLEAN.notNull());
        public final Field<LocalDateTime> REVOKED_AT = DSL.field(DSL.name("revoked_at"), org.jooq.impl.SQLDataType.LOCALDATETIME);
        public final Field<LocalDateTime> CREATED_AT = DSL.field(DSL.name("created_at"), org.jooq.impl.SQLDataType.LOCALDATETIME.notNull());

        public RefreshTokens() {
            super(DSL.name("auth", "refresh_tokens"));
        }
    }
}
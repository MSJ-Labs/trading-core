package com.msj.infrastructure.jooq;

import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 * JOOQ table references for user management
 * Manually created since code generation isn't working
 */
public class Tables {

    // Users table
    public static final Users USERS = Users.USERS;

    // Roles table
    public static final Roles ROLES = Roles.ROLES;

    // Permissions table
    public static final Permissions PERMISSIONS = Permissions.PERMISSIONS;

    // User roles junction table
    public static final UserRoles USER_ROLES = UserRoles.USER_ROLES;

    // Role permissions junction table
    public static final RolePermissions ROLE_PERMISSIONS = RolePermissions.ROLE_PERMISSIONS;

    // User profiles table
    public static final UserProfiles USER_PROFILES = UserProfiles.USER_PROFILES;

    // Crypto table
    public static final Crypto CRYPTO = Crypto.CRYPTO;

    // Table classes
    public static class Users extends TableImpl<org.jooq.Record> {
        public static final Users USERS = new Users();
        public final org.jooq.TableField<org.jooq.Record, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> USERNAME = createField("username", org.jooq.impl.SQLDataType.VARCHAR(100), this);
        public final org.jooq.TableField<org.jooq.Record, String> EMAIL = createField("email", org.jooq.impl.SQLDataType.VARCHAR(255), this);
        public final org.jooq.TableField<org.jooq.Record, String> PASSWORD = createField("password", org.jooq.impl.SQLDataType.VARCHAR(255), this);
        public final org.jooq.TableField<org.jooq.Record, String> FIRST_NAME = createField("first_name", org.jooq.impl.SQLDataType.VARCHAR(100), this);
        public final org.jooq.TableField<org.jooq.Record, String> LAST_NAME = createField("last_name", org.jooq.impl.SQLDataType.VARCHAR(100), this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> ENABLED = createField("enabled", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> ACCOUNT_NON_EXPIRED = createField("account_non_expired", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> ACCOUNT_NON_LOCKED = createField("account_non_locked", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> CREDENTIALS_NON_EXPIRED = createField("credentials_non_expired", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> UPDATED_AT = createField("updated_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> LAST_LOGIN_AT = createField("last_login_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);
        public final org.jooq.TableField<org.jooq.Record, Integer> FAILED_LOGIN_ATTEMPTS = createField("failed_login_attempts", org.jooq.impl.SQLDataType.INTEGER, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> LOCKED_UNTIL = createField("locked_until", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private Users() { super(DSL.name("users")); }
    }

    public static class Roles extends TableImpl<org.jooq.Record> {
        public static final Roles ROLES = new Roles();
        public final org.jooq.TableField<org.jooq.Record, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(50), this);
        public final org.jooq.TableField<org.jooq.Record, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR(255), this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private Roles() { super(DSL.name("roles")); }
    }

    public static class Permissions extends TableImpl<org.jooq.Record> {
        public static final Permissions PERMISSIONS = new Permissions();
        public final org.jooq.TableField<org.jooq.Record, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(100), this);
        public final org.jooq.TableField<org.jooq.Record, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR(255), this);
        public final org.jooq.TableField<org.jooq.Record, String> RESOURCE = createField("resource", org.jooq.impl.SQLDataType.VARCHAR(100), this);
        public final org.jooq.TableField<org.jooq.Record, String> ACTION = createField("action", org.jooq.impl.SQLDataType.VARCHAR(50), this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private Permissions() { super(DSL.name("permissions")); }
    }

    public static class UserRoles extends TableImpl<org.jooq.Record> {
        public static final UserRoles USER_ROLES = new UserRoles();
        public final org.jooq.TableField<org.jooq.Record, String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> ROLE_ID = createField("role_id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> ASSIGNED_AT = createField("assigned_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private UserRoles() { super(DSL.name("user_roles")); }
    }

    public static class RolePermissions extends TableImpl<org.jooq.Record> {
        public static final RolePermissions ROLE_PERMISSIONS = new RolePermissions();
        public final org.jooq.TableField<org.jooq.Record, String> ROLE_ID = createField("role_id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> PERMISSION_ID = createField("permission_id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> ASSIGNED_AT = createField("assigned_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private RolePermissions() { super(DSL.name("role_permissions")); }
    }

    public static class UserProfiles extends TableImpl<org.jooq.Record> {
        public static final UserProfiles USER_PROFILES = new UserProfiles();
        public final org.jooq.TableField<org.jooq.Record, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> TIMEZONE = createField("timezone", org.jooq.impl.SQLDataType.VARCHAR(50), this);
        public final org.jooq.TableField<org.jooq.Record, String> LANGUAGE = createField("language", org.jooq.impl.SQLDataType.VARCHAR(10), this);
        public final org.jooq.TableField<org.jooq.Record, String> THEME = createField("theme", org.jooq.impl.SQLDataType.VARCHAR(20), this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> NOTIFICATIONS_ENABLED = createField("notifications_enabled", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, Boolean> TWO_FACTOR_ENABLED = createField("two_factor_enabled", org.jooq.impl.SQLDataType.BOOLEAN, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> UPDATED_AT = createField("updated_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private UserProfiles() { super(DSL.name("user_profiles")); }
    }

    public static class Crypto extends TableImpl<org.jooq.Record> {
        public static final Crypto CRYPTO = new Crypto();
        public final org.jooq.TableField<org.jooq.Record, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36), this);
        public final org.jooq.TableField<org.jooq.Record, String> SYMBOL = createField("symbol", org.jooq.impl.SQLDataType.VARCHAR(10), this);
        public final org.jooq.TableField<org.jooq.Record, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255), this);
        public final org.jooq.TableField<org.jooq.Record, java.math.BigDecimal> CURRENT_PRICE = createField("current_price", org.jooq.impl.SQLDataType.DECIMAL(20, 8), this);
        public final org.jooq.TableField<org.jooq.Record, java.math.BigDecimal> MARKET_CAP = createField("market_cap", org.jooq.impl.SQLDataType.DECIMAL(30, 8), this);
        public final org.jooq.TableField<org.jooq.Record, java.math.BigDecimal> VOLUME_24H = createField("volume_24h", org.jooq.impl.SQLDataType.DECIMAL(20, 8), this);
        public final org.jooq.TableField<org.jooq.Record, java.math.BigDecimal> CHANGE_PERCENT_24H = createField("change_percent_24h", org.jooq.impl.SQLDataType.DECIMAL(10, 4), this);
        public final org.jooq.TableField<org.jooq.Record, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR(1000), this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);
        public final org.jooq.TableField<org.jooq.Record, java.time.LocalDateTime> UPDATED_AT = createField("updated_at", org.jooq.impl.SQLDataType.LOCALDATETIME, this);

        private Crypto() { super(DSL.name("crypto")); }
    }
}

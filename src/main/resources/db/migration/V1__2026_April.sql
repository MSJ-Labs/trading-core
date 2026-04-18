-- Layer 1: auth schema — users, roles, permissions, user_roles, role_permissions, user_profiles
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrator with full access'),
(2, 'ROLE_USER', 'Regular user with basic access'),
(3, 'ROLE_TRADER', 'Trading user with crypto access');

INSERT INTO permissions (id, name, description, resource, action) VALUES
(10, 'CRYPTO_READ',        'Read crypto data',          'CRYPTO', 'READ'),
(11, 'CRYPTO_CREATE',      'Create crypto assets',      'CRYPTO', 'CREATE'),
(12, 'CRYPTO_UPDATE',      'Update crypto assets',      'CRYPTO', 'UPDATE'),
(13, 'CRYPTO_DELETE',      'Delete crypto assets',      'CRYPTO', 'DELETE'),
(20, 'USER_READ',          'Read user data',            'USER',   'READ'),
(21, 'USER_CREATE',        'Create users',              'USER',   'CREATE'),
(22, 'USER_UPDATE',        'Update users',              'USER',   'UPDATE'),
(23, 'USER_DELETE',        'Delete users',              'USER',   'DELETE'),
(30, 'ADMIN_FULL_ACCESS',  'Full administrative access','ADMIN',  'FULL_ACCESS');

INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 10), (1, 11), (1, 12), (1, 13),
(1, 20), (1, 21), (1, 22), (1, 23), (1, 30),
(3, 10), (3, 11), (3, 12),
(2, 10), (2, 20);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
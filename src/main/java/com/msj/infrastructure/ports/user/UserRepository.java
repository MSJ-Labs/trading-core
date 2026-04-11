package com.msj.infrastructure.ports.user;

import com.msj.domain.user.User;
import com.msj.domain.user.UserId;

import java.util.Optional;

/**
 * Port interface for User persistence
 * Following SOLID: Interface Segregation (only user operations)
 */
public interface UserRepository {

    /**
     * Save or update a user
     */
    User save(User user);

    /**
     * Find user by ID
     */
    Optional<User> findById(UserId id);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Delete user by ID
     */
    void deleteById(UserId id);

    /**
     * Check if user exists by ID
     */
    boolean existsById(UserId id);
}

package com.msj.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Role entity representing user roles
 * Following SOLID: Single Responsibility (role domain logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private RoleId id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    // Relationships
    private Set<Permission> permissions;

    /**
     * Factory method to create a new role
     */
    public static Role create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        return Role.builder()
                .id(RoleId.generate())
                .name(name.trim().toUpperCase())
                .description(description != null ? description.trim() : null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if role has a specific permission
     */
    public boolean hasPermission(String permissionName) {
        if (permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * Check if role has permission for resource and action
     */
    public boolean hasPermission(String resource, String action) {
        if (permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(permission ->
                    permission.getResource().equals(resource) &&
                    permission.getAction().equals(action));
    }

    /**
     * Get role name with ROLE_ prefix for Spring Security
     */
    public String getRoleName() {
        return name.startsWith("ROLE_") ? name : "ROLE_" + name;
    }
}

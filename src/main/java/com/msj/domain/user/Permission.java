package com.msj.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Permission entity representing fine-grained access rights
 * Following SOLID: Single Responsibility (permission domain logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private PermissionId id;
    private String name;
    private String description;
    private String resource;
    private String action;
    private LocalDateTime createdAt;

    /**
     * Factory method to create a new permission
     */
    public static Permission create(String name, String description, String resource, String action) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be null or empty");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }

        return Permission.builder()
                .id(PermissionId.generate())
                .name(name.trim().toUpperCase())
                .description(description != null ? description.trim() : null)
                .resource(resource.trim().toUpperCase())
                .action(action.trim().toUpperCase())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get permission string for Spring Security
     */
    public String getPermissionString() {
        return resource + ":" + action;
    }

    /**
     * Check if permission matches resource and action
     */
    public boolean matches(String resource, String action) {
        return this.resource.equals(resource.toUpperCase()) &&
               this.action.equals(action.toUpperCase());
    }
}

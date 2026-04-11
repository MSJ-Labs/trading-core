package com.msj.controller.mapper;

import com.msj.controller.dto.*;
import com.msj.domain.user.Role;
import com.msj.domain.user.User;
import com.msj.domain.user.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for User domain objects
 * Configured to work with Lombok builders
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {

    /**
     * Convert RegisterRequest to User domain object
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    User toUser(RegisterRequest request);

    /**
     * Convert User domain object to UserResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toUserResponse(User user);

    /**
     * Convert User domain object to UserProfileResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "timezone", expression = "java(user.getProfile() != null ? user.getProfile().getTimezone() : \"UTC\")")
    @Mapping(target = "language", expression = "java(user.getProfile() != null ? user.getProfile().getLanguage() : \"en\")")
    @Mapping(target = "theme", expression = "java(user.getProfile() != null ? user.getProfile().getTheme() : \"light\")")
    @Mapping(target = "notificationsEnabled", expression = "java(user.getProfile() != null ? user.getProfile().isNotificationsEnabled() : true)")
    @Mapping(target = "twoFactorEnabled", expression = "java(user.getProfile() != null ? user.getProfile().isTwoFactorEnabled() : false)")
    UserProfileResponse toUserProfileResponse(User user);

    /**
     * Convert Role domain object to RoleResponse DTO
     */
    RoleResponse toRoleResponse(Role role);

    /**
     * Convert list of Role objects to list of RoleResponse DTOs
     */
    List<RoleResponse> toRoleResponses(List<Role> roles);

    /**
     * Update User entity from UpdateUserRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Update UserProfile from UpdateProfileRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget UserProfile profile);
}

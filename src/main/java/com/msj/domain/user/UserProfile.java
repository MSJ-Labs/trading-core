package com.msj.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserProfile entity for user preferences and settings
 * Following SOLID: Single Responsibility (profile domain logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private UserProfileId id;
    private UserId userId;
    private String timezone;
    private String language;
    private String theme;
    private boolean notificationsEnabled;
    private boolean twoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a new user profile
     */
    public static UserProfile create(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }

        return UserProfile.builder()
                .id(UserProfileId.generate())
                .userId(userId)
                .timezone("UTC")
                .language("en")
                .theme("light")
                .notificationsEnabled(true)
                .twoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Update profile settings
     */
    public void update(String timezone, String language, String theme,
                      Boolean notificationsEnabled, Boolean twoFactorEnabled) {
        if (timezone != null && !timezone.trim().isEmpty()) {
            this.timezone = timezone.trim();
        }
        if (language != null && !language.trim().isEmpty()) {
            this.language = language.trim();
        }
        if (theme != null && !theme.trim().isEmpty()) {
            this.theme = theme.trim();
        }
        if (notificationsEnabled != null) {
            this.notificationsEnabled = notificationsEnabled;
        }
        if (twoFactorEnabled != null) {
            this.twoFactorEnabled = twoFactorEnabled;
        }
        this.updatedAt = LocalDateTime.now();
    }
}

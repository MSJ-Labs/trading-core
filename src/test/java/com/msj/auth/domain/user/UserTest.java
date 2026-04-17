package com.msj.auth.domain.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.msj.auth.support.UserTestFactory.expiredLockUser;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void register_createsEnabledUser() {
        User user = User.register("jdoe", "j@doe.com", "hashed", "John", "Doe");

        assertThat(user.getUsername()).isEqualTo("jdoe");
        assertThat(user.getEmail()).isEqualTo("j@doe.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getRoles()).containsExactly("ROLE_USER");
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void register_normalizesEmailToLowerCase() {
        User user = User.register("jdoe", "J@DOE.COM", "hashed", null, null);
        assertThat(user.getEmail()).isEqualTo("j@doe.com");
    }

    @Test
    void register_rejectsBlankUsername() {
        assertThatThrownBy(() -> User.register("  ", "j@doe.com", "hashed", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_rejectsBlankEmail() {
        assertThatThrownBy(() -> User.register("jdoe", "", "hashed", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_rejectsBlankPassword() {
        assertThatThrownBy(() -> User.register("jdoe", "j@doe.com", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordSuccessfulLogin_resetsFailedAttemptsAndUnlocks() {
        User user = User.register("jdoe", "j@doe.com", "hashed", null, null);
        user.recordFailedLoginAttempt();
        user.recordFailedLoginAttempt();

        user.recordSuccessfulLogin();

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void recordFailedLoginAttempt_incrementsCounter() {
        User user = User.register("jdoe", "j@doe.com", "hashed", null, null);

        user.recordFailedLoginAttempt();
        user.recordFailedLoginAttempt();

        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void recordFailedLoginAttempt_locksAccountAtFiveAttempts() {
        User user = User.register("jdoe", "j@doe.com", "hashed", null, null);

        for (int i = 0; i < 5; i++) {
            user.recordFailedLoginAttempt();
        }

        assertThat(user.isAccountNonLocked()).isFalse();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(29));
    }

    @Test
    void isCurrentlyLocked_returnsFalseForUnlockedUser() {
        User user = User.register("jdoe", "j@doe.com", "hashed", null, null);
        assertThat(user.isCurrentlyLocked()).isFalse();
    }

    @Test
    void isCurrentlyLocked_autoUnlocksAfterLockPeriodExpires() {
        User user = expiredLockUser("jdoe");

        assertThat(user.isCurrentlyLocked()).isFalse();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void changePassword_updatesHash() {
        User user = User.register("jdoe", "j@doe.com", "old-hash", null, null);

        user.changePassword("new-hash");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void changePassword_rejectsBlank() {
        User user = User.register("jdoe", "j@doe.com", "hashed", null, null);

        assertThatThrownBy(() -> user.changePassword("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFullName_returnsCorrectFormat() {
        User both = User.register("jdoe", "j@doe.com", "h", "John", "Doe");
        assertThat(both.getFullName()).isEqualTo("John Doe");

        User firstOnly = User.register("jdoe2", "j2@doe.com", "h", "John", null);
        assertThat(firstOnly.getFullName()).isEqualTo("John");

        User lastOnly = User.register("jdoe3", "j3@doe.com", "h", null, "Doe");
        assertThat(lastOnly.getFullName()).isEqualTo("Doe");

        User neitherName = User.register("jdoe4", "j4@doe.com", "h", null, null);
        assertThat(neitherName.getFullName()).isEqualTo("jdoe4");
    }
}
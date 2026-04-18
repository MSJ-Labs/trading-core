package com.msj.auth.infrastructure.security;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import java.util.Set;

import static com.msj.auth.support.UserTestFactory.activeUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void wrapsUserDetailsCorrectly() {
        User user = activeUser("jdoe");
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getUsername()).isEqualTo("jdoe");
        assertThat(principal.getPassword()).isEqualTo("$hashed$");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.getUser()).isSameAs(user);
    }

    @Test
    void mapsRolesToGrantedAuthorities() {
        User user = activeUser("jdoe");
        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER");
    }

    @Test
    void nullRoles_producesEmptyAuthorities() {
        User user = User.builder()
                .id(UserId.generate())
                .username("jdoe")
                .email("j@doe.com")
                .passwordHash("hashed")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(null)
                .build();

        assertThat(new UserPrincipal(user).getAuthorities()).isEmpty();
    }

    @Test
    void tokenConstructor_buildsLightweightPrincipalWithoutUser() {
        UserPrincipal principal = new UserPrincipal("jdoe", Set.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(principal.getUsername()).isEqualTo("jdoe");
        assertThat(principal.getUser()).isNull();
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void disabledUser_reflectsInPrincipal() {
        User user = User.builder()
                .id(UserId.generate())
                .username("jdoe")
                .email("j@doe.com")
                .passwordHash("hashed")
                .enabled(false)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        assertThat(new UserPrincipal(user).isEnabled()).isFalse();
    }
}
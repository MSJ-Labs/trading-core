package com.msj.auth.infrastructure.security;

import com.msj.auth.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security adapter — wraps the domain User.
 * Keeps UserDetails (infrastructure concern) out of the domain.
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final User user;
    private final Set<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.user = user;
        this.authorities = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet());
    }

    @Override public String getUsername() { return user.getUsername(); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return user.isEnabled(); }
    @Override public boolean isAccountNonExpired() { return user.isAccountNonExpired(); }
    @Override public boolean isAccountNonLocked() { return !user.isCurrentlyLocked(); }
    @Override public boolean isCredentialsNonExpired() { return user.isCredentialsNonExpired(); }
}
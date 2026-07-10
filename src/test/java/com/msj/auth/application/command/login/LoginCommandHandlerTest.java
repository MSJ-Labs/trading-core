package com.msj.auth.application.command.login;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserNotFoundException;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import com.msj.auth.infrastructure.ports.UserRepository;
import com.msj.auth.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.msj.auth.support.UserTestFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginCommandHandlerTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @InjectMocks LoginCommandHandler handler;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = activeUser("jdoe");
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("pass", "$hashed$")).thenReturn(true);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(eq("jdoe"), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(eq("jdoe"), any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationFromToken("refresh-token")).thenReturn(LocalDateTime.now().plusDays(7));

        LoginResult result = handler.handle(new LoginCommand("jdoe", "pass"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().getUsername()).isEqualTo("jdoe");
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginCommand command = new LoginCommand("unknown", "pass");
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void login_throwsOnBadPassword() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "$hashed$")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginCommand command = new LoginCommand("jdoe", "wrong");
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(activeUser.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void login_locksAccountAfterFiveFailures() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginCommand command = new LoginCommand("jdoe", "wrong");
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(BadCredentialsException.class);
        }

        assertThat(activeUser.isCurrentlyLocked()).isTrue();
        assertThat(activeUser.getLockedUntil()).isNotNull();
    }

    @Test
    void login_throwsWhenAccountLocked() {
        when(userRepository.findByUsername("locked")).thenReturn(Optional.of(lockedUser()));

        LoginCommand command = new LoginCommand("locked", "pass");
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(LockedException.class);
    }
}
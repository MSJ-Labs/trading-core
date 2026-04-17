package com.msj.auth.application.command.register;

import com.msj.auth.domain.user.EmailAlreadyExistsException;
import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UsernameAlreadyExistsException;
import com.msj.auth.infrastructure.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCommandHandlerTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks RegisterCommandHandler handler;

    private RegisterCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new RegisterCommand("jdoe", "jdoe@example.com", "secret123", "John", "Doe");
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$hashed$");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = handler.handle(validCommand);

        assertThat(result.getUsername()).isEqualTo("jdoe");
        assertThat(result.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("$hashed$");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getRoles()).contains("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(validCommand))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("jdoe");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(validCommand))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("jdoe@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_encodesPassword() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("bcrypt_hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = handler.handle(validCommand);

        assertThat(result.getPasswordHash()).isEqualTo("bcrypt_hash");
        verify(passwordEncoder).encode("secret123");
    }
}
package com.msj.auth.application.command.register;

import com.msj.auth.domain.user.EmailAlreadyExistsException;
import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UsernameAlreadyExistsException;
import com.msj.auth.infrastructure.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User handle(RegisterCommand command) {
        log.info("Registering new user: {}", command.username());

        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());

        User user = User.register(
                command.username(),
                command.email(),
                passwordHash,
                command.firstName(),
                command.lastName()
        );

        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", saved.getId().value());
        return saved;
    }
}
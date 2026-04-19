package com.msj.auth.application.command.login;

import com.msj.auth.domain.token.TokenHasher;
import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserNotFoundException;
import com.msj.auth.infrastructure.ports.RefreshTokenRepository;
import com.msj.auth.infrastructure.ports.UserRepository;
import com.msj.auth.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResult handle(LoginCommand command) {
        log.info("Login attempt for user: {}", command.username());

        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        if (user.isCurrentlyLocked()) {
            throw new LockedException("Account is locked. Try again later.");
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            log.warn("Failed login attempt for user: {} ({} attempts)",
                    command.username(), user.getFailedLoginAttempts());
            throw new BadCredentialsException("Invalid credentials");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRoles());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRoles());

        refreshTokenRepository.save(
                TokenHasher.hash(refreshToken),
                user.getId(),
                jwtTokenProvider.getExpirationFromToken(refreshToken)
        );

        log.info("User logged in successfully: {}", command.username());
        return new LoginResult(accessToken, refreshToken, user);
    }
}
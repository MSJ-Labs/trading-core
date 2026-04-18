package com.msj.auth.application.query;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserNotFoundException;
import com.msj.auth.infrastructure.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserProfileQueryHandler {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User handle(GetUserProfileQuery query) {
        return userRepository.findByUsername(query.username())
                .orElseThrow(() -> UserNotFoundException.forUsername(query.username()));
    }
}
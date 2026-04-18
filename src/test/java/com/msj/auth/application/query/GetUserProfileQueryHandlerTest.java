package com.msj.auth.application.query;

import com.msj.auth.domain.user.User;
import com.msj.auth.domain.user.UserNotFoundException;
import com.msj.auth.infrastructure.ports.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.msj.auth.support.UserTestFactory.activeUser;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserProfileQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserProfileQueryHandler handler;

    @Test
    void handle_userExists_returnsUser() {
        User user = activeUser("jdoe");
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(user));

        User result = handler.handle(new GetUserProfileQuery("jdoe"));

        assertThat(result.getUsername()).isEqualTo("jdoe");
    }

    @Test
    void handle_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetUserProfileQuery("unknown")))
                .isInstanceOf(UserNotFoundException.class);
    }
}
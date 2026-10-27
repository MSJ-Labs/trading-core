package com.msj.identity.application.query;

import com.msj.identity.application.port.out.CurrentUserProvider;
import com.msj.identity.application.result.CurrentUser;
import com.msj.identity.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentIdentityQueryHandlerTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private GetCurrentIdentityQueryHandler handler;

    @Test
    void handle_returnsTheCurrentUser() {
        CurrentUser user = new CurrentUser(new UserId(UUID.randomUUID()), Set.of("USER"));
        when(currentUserProvider.currentUser()).thenReturn(user);

        assertThat(handler.handle()).isEqualTo(user);
    }
}
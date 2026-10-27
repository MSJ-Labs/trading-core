package com.msj.identity.application.query;

import com.msj.identity.application.port.out.CurrentUserProvider;
import com.msj.identity.application.result.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentIdentityQueryHandler {

    private final CurrentUserProvider currentUserProvider;

    public CurrentUser handle() {
        return currentUserProvider.currentUser();
    }
}
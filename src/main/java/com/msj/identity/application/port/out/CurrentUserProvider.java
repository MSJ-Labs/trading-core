package com.msj.identity.application.port.out;

import com.msj.identity.application.result.CurrentUser;

/**
 * The caller of the current use case. Implemented by an adapter that translates the authenticated principal into the
 * identity context's own types; it refuses callers it cannot identify.
 */
public interface CurrentUserProvider {

    CurrentUser currentUser();
}
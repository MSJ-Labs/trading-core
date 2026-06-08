package com.msj.auth.application.command.logout;

public record LogoutCommand(String username, String rawRefreshToken) {}
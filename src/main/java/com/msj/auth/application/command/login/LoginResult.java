package com.msj.auth.application.command.login;

import com.msj.auth.domain.user.User;

public record LoginResult(String accessToken, String refreshToken, User user) {}
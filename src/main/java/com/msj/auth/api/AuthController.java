package com.msj.auth.api;

import com.msj.auth.api.dto.LoginRequest;
import com.msj.auth.api.dto.RegisterRequest;
import com.msj.auth.api.dto.UserProfileResponse;
import com.msj.auth.application.command.login.LoginCommand;
import com.msj.auth.application.command.login.LoginCommandHandler;
import com.msj.auth.application.command.login.LoginResult;
import com.msj.auth.application.command.logout.LogoutCommandHandler;
import com.msj.auth.application.command.refresh.RefreshTokenCommand;
import com.msj.auth.application.command.refresh.RefreshTokenCommandHandler;
import com.msj.auth.application.command.register.RegisterCommand;
import com.msj.auth.application.command.register.RegisterCommandHandler;
import com.msj.auth.infrastructure.security.JwtCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterCommandHandler registerHandler;
    private final LoginCommandHandler loginHandler;
    private final LogoutCommandHandler logoutHandler;
    private final RefreshTokenCommandHandler refreshHandler;
    private final JwtCookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = registerHandler.handle(new RegisterCommand(
                request.username(), request.email(), request.password(),
                request.firstName(), request.lastName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserProfileResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletResponse response) {
        LoginResult result = loginHandler.handle(new LoginCommand(request.username(), request.password()));
        cookieService.addAuthCookies(response, result.accessToken(), result.refreshToken());
        return ResponseEntity.ok(UserProfileResponse.from(result.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails principal,
                                        HttpServletResponse response) {
        String username = principal != null ? principal.getUsername() : "anonymous";
        logoutHandler.handle(username, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshToken(request);
        String newAccessToken = refreshHandler.handle(new RefreshTokenCommand(refreshToken));
        cookieService.refreshAccessTokenCookie(response, newAccessToken);
        return ResponseEntity.noContent().build();
    }
}
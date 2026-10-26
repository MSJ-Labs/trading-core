package com.msj.shared.infrastructure.security;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

/**
 * Injects the caller into controller methods. Anonymous access:
 * <ul>
 *   <li>{@code AuthenticatedPrincipal p}: the endpoint requires authentication, anonymous callers are refused;</li>
 *   <li>{@code Optional<AuthenticatedPrincipal> p} or a {@code @Nullable} parameter: public endpoint that may
 *       personalise its answer, anonymous callers get an empty value.</li>
 * </ul>
 * Which URLs are public is decided in the security filter chain, not here.
 */
class AuthenticatedPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final CurrentAuthenticatedPrincipal currentPrincipal;

    AuthenticatedPrincipalArgumentResolver(CurrentAuthenticatedPrincipal currentPrincipal) {
        this.currentPrincipal = currentPrincipal;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthenticatedPrincipal.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        Optional<AuthenticatedPrincipal> principal = currentPrincipal.get();

        if (parameter.getParameterType() == Optional.class) {
            return principal;
        }
        if (principal.isPresent() || parameter.isOptional()) {
            return principal.orElse(null);
        }
        throw new AuthenticationCredentialsNotFoundException("Authentication required");
    }
}
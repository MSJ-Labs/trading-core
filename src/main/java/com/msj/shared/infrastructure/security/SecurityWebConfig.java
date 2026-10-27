package com.msj.shared.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class SecurityWebConfig implements WebMvcConfigurer {

    private final CurrentAuthenticatedPrincipal currentPrincipal;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticatedPrincipalArgumentResolver(currentPrincipal));
    }
}
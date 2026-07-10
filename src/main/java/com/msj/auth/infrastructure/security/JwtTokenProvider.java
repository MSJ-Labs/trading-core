package com.msj.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLES = "roles";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(String username, Set<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .claim(CLAIM_ROLES, roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationMs))
                .signWith(signingKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(String username, Set<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .claim(CLAIM_ROLES, roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationMs))
                .signWith(signingKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        List<String> roles = (List<String>) parseClaims(token).get(CLAIM_ROLES);
        return roles != null ? Set.copyOf(roles) : Set.of();
    }

    public LocalDateTime getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
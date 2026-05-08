package com.example.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenTtl;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${gateway.secret}") String secret,
            @Value("${gateway.access-token-ttl}") long accessTokenTtl,
            @Value("${gateway.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.issuer = issuer;
    }

    public String generateAccessToken(Long userId, String userType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtl);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userType", userType)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            validateTokenAndGetClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(validateTokenAndGetClaims(token).getSubject());
    }

    public String getUserTypeFromToken(String token) {
        return validateTokenAndGetClaims(token).get("userType", String.class);
    }
}
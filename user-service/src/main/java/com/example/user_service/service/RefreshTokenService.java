package com.example.user_service.service;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "my-secret-at-least-256-bits-long-for-jwt".getBytes(StandardCharsets.UTF_8)
    );

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createRefreshToken(Long userId, UserType userType, long ttlMillis) {
        Instant expiresInstant = Instant.now().plusMillis(ttlMillis);

        String jwt = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim("userType", userType.name())
                .issuedAt(new Date())
                .expiration(Date.from(expiresInstant))
                .signWith(secretKey)
                .compact();

        String tokenHash = hashToken(jwt);

        RefreshToken token = new RefreshToken();
        token.setTokenHash(tokenHash);
        token.setUserId(userId);
        token.setUserType(userType);
        token.setExpiresAt(expiresInstant.atZone(ZoneId.systemDefault()).toLocalDateTime());
        refreshTokenRepository.save(token);

        return jwt;
    }

    public RefreshToken validateRefreshToken(String rawToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(rawToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token", e);
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found (revoked?)"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return token;
    }

    @Transactional
    public void deleteUserTokens(Long userId, UserType userType) {
        refreshTokenRepository.deleteByUserIdAndUserType(userId, userType);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
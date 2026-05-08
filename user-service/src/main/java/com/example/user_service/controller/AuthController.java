package com.example.user_service.controller;

import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RefreshTokenRequest;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    @Value("${gateway.secret}")
    private String jwtSecret;

    @Value("${gateway.access-token-ttl}")
    private long accessTokenTtl;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        // Временная заглушка аутентификации
        if (!"password".equals(request.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        // TODO: найти реального пользователя по email в БД
        Long userId = 1L;
        UserType userType = UserType.PASSENGER;

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtl);

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("userType", userType.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        String refreshToken = refreshTokenService.createRefreshToken(userId, userType, 7 * 24 * 3600_000L);

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var refreshTokenEntity = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        Long userId = refreshTokenEntity.getUserId();
        UserType userType = refreshTokenEntity.getUserType();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtl);

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("userType", userType.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        return ResponseEntity.ok(new TokenResponse(accessToken, request.getRefreshToken()));
    }
}
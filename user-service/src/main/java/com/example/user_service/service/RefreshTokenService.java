package com.example.user_service.service;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.repository.RefreshTokenRepository;
import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken CreateRefreshToken(Long userId, RefreshToken.UserType userType, String rawToken, Long ttlMillis){
        String tokenHash = DigestUtils.sha256Hex(rawToken.getBytes(StandardCharsets.UTF_8));
        RefreshToken token = new RefreshToken();
        token.setTokenHash(tokenHash);
        token.setUserId(userId);
        token.setUserType(userType);
        token.setExpiresAt(LocalDateTime.now().plusNanos(ttlMillis * 1_000_000));
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public void deleteUserTokens(Long userId, RefreshToken.UserType userType){
        refreshTokenRepository.deleteByUserIdAndUserType(userId, userType);
    }

    public RefreshToken validationRefreshToken(String rawToken){
        String tokenHash = DigestUtils.sha256Hex(rawToken.getBytes(StandardCharsets.UTF_8));
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Токен hash не найден или не действителен"));
        if(token.getExpiresAt().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Токен истёк");
        }
        return token;
    }
}

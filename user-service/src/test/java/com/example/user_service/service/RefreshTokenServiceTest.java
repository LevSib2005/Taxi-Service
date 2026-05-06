package com.example.user_service.service;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository repository;

    @InjectMocks
    RefreshTokenService service;

    @Test
    void createRefreshToken_shouldReturnJwtAndSaveHash() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String jwt = service.createRefreshToken(1L, UserType.PASSENGER, 60000L);

        assertThat(jwt).isNotBlank();
        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_shouldReturnTokenIfValid() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        String jwt = service.createRefreshToken(1L, UserType.PASSENGER, 60000L);

        String hash = hashToken(jwt);

        RefreshToken stored = new RefreshToken();
        stored.setTokenHash(hash);
        stored.setUserId(1L);
        stored.setUserType(UserType.PASSENGER);
        stored.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(stored));

        RefreshToken result = service.validateRefreshToken(jwt);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTokenHash()).isEqualTo(hash);
    }

    @Test
    void validateRefreshToken_shouldThrowIfJwtInvalid() {
        assertThatThrownBy(() -> service.validateRefreshToken("bad-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid refresh token");
    }

    @Test
    void validateRefreshToken_shouldThrowIfNotInDb() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        String jwt = service.createRefreshToken(1L, UserType.PASSENGER, 60000L);

        String hash = hashToken(jwt);
        when(repository.findByTokenHash(hash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateRefreshToken(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token not found (revoked?)");
    }

    @Test
    void deleteUserTokens_shouldCallRepository() {
        service.deleteUserTokens(2L, UserType.DRIVER);
        verify(repository).deleteByUserIdAndUserType(2L, UserType.DRIVER);
    }

    private String hashToken(String rawToken) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
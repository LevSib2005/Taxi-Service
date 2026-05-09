package com.example.user_service.controller;

import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RefreshTokenRequest;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.Driver;
import com.example.user_service.entity.Passenger;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.security.JwtTokenProvider;
import com.example.user_service.service.DriverService;
import com.example.user_service.service.PassengerService;
import com.example.user_service.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация пользователей")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PassengerService passengerService;
    private final DriverService driverService;

    @Operation(summary = "Войти в систему (по email)")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Passenger passenger = passengerService.getPassengerByEmail(request.getEmail());
            TokenResponse tokens = passengerService.generateTokensForPassenger(passenger.getId());
            log.debug("Passenger logged in: {}", request.getEmail());
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Driver driver = driverService.getDriverByEmail(request.getEmail());
            TokenResponse tokens = driverService.generateTokensForDriver(driver.getId());
            log.debug("Driver logged in: {}", request.getEmail());
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException ignored) {
        }

        log.warn("Login failed for email: {}", request.getEmail());
        return ResponseEntity.status(401).build();
    }

    @Operation(summary = "Обновить access token используя refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            var refreshTokenEntity = refreshTokenService.validateRefreshToken(request.getRefreshToken());
            Long userId = refreshTokenEntity.getUserId();
            UserType userType = refreshTokenEntity.getUserType();

            String accessToken = jwtTokenProvider.generateAccessToken(userId, userType.name());
            return ResponseEntity.ok(new TokenResponse(accessToken, request.getRefreshToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @Operation(summary = "Выйти из системы")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            var refreshTokenEntity = refreshTokenService.validateRefreshToken(request.getRefreshToken());
            refreshTokenService.deleteUserTokens(
                    refreshTokenEntity.getUserId(),
                    refreshTokenEntity.getUserType()
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
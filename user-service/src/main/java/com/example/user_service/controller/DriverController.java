package com.example.user_service.controller;

import com.example.user_service.dto.DriverRequest;
import com.example.user_service.dto.DriverResponse;
import com.example.user_service.dto.DriverStatusUpdateRequest;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.Driver;
import com.example.user_service.mapper.DriverMapper;
import com.example.user_service.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Управление водителями")
public class DriverController {

    private final DriverService driverService;
    private final DriverMapper mapper;

    @Operation(summary = "Зарегистрировать водителя")
    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody DriverRequest request) {
        Driver driver = driverService.registerDriver(request);
        TokenResponse tokenResponse = driverService.generateTokensForDriver(driver.getId());

        DriverResponse driverResponse = mapper.toResponse(driver);

        Map<String, Object> response = new HashMap<>();
        response.put("driver", driverResponse);
        response.put("accessToken", tokenResponse.getAccessToken());
        response.put("refreshToken", tokenResponse.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Получить профиль водителя по ID",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> get(@PathVariable Long id, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();

        // Водитель может получить только свой профиль
        if (!authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Driver driver = driverService.getDriverById(id);
            DriverResponse response = mapper.toResponse(driver);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Обновить статус водителя",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverStatusUpdateRequest request,
            Authentication authentication) {

        Long authenticatedUserId = (Long) authentication.getPrincipal();

        // Водитель может обновить только свой статус
        if (!authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Driver driver = driverService.updateDriverStatus(id, request.getStatus());
            DriverResponse response = mapper.toResponse(driver);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
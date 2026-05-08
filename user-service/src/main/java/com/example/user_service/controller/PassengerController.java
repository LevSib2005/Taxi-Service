package com.example.user_service.controller;

import com.example.user_service.dto.PassengerRequest;
import com.example.user_service.dto.PassengerResponse;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.Passenger;
import com.example.user_service.mapper.PassengerMapper;
import com.example.user_service.service.PassengerService;
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
@RequestMapping("/passengers")
@RequiredArgsConstructor
@Tag(name = "Passengers", description = "Управление пассажирами")
public class PassengerController {

    private final PassengerService passengerService;
    private final PassengerMapper mapper;

    @Operation(summary = "Зарегистрировать пассажира")
    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody PassengerRequest request) {
        Passenger passenger = passengerService.registerPassenger(request);
        TokenResponse tokenResponse = passengerService.generateTokensForPassenger(passenger.getId());

        PassengerResponse passengerResponse = mapper.toResponse(passenger);

        Map<String, Object> response = new HashMap<>();
        response.put("passenger", passengerResponse);
        response.put("accessToken", tokenResponse.getAccessToken());
        response.put("refreshToken", tokenResponse.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Получить профиль пассажира по ID",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassengerResponse> get(@PathVariable Long id, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();

        // Пассажир может получить только свой профиль
        if (!authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Passenger passenger = passengerService.getPassengerById(id);
            PassengerResponse response = mapper.toResponse(passenger);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
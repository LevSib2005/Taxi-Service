package com.example.user_service.controller;

import com.example.user_service.dto.DriverRequest;
import com.example.user_service.dto.DriverResponse;
import com.example.user_service.dto.DriverStatusUpdateRequest;
import com.example.user_service.entity.Driver;
import com.example.user_service.mapper.DriverMapper;
import com.example.user_service.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
@Tag(name = "Drivers", description = "Управление водителями")
public class DriverController {

    private final DriverService driverService;
    private final DriverMapper mapper;

    public DriverController(DriverService driverService, DriverMapper mapper) {
        this.driverService = driverService;
        this.mapper = mapper;
    }

    @Operation(summary = "Зарегистрировать водителя")
    @PostMapping
    public ResponseEntity<DriverResponse> register(@Valid @RequestBody DriverRequest request) {
        Driver driver = driverService.registerDriver(request);
        DriverResponse response = mapper.toResponse(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить профиль водителя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> get(@PathVariable Long id) {
        try {
            Driver driver = driverService.getDriverById(id);
            DriverResponse response = mapper.toResponse(driver);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить статус водителя")
    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverStatusUpdateRequest request) {
        try {
            Driver driver = driverService.updateDriverStatus(id, request.getStatus());
            DriverResponse response = mapper.toResponse(driver);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
package com.example.user_service.controller;

import com.example.user_service.dto.PassengerRequest;
import com.example.user_service.dto.PassengerResponse;
import com.example.user_service.entity.Passenger;
import com.example.user_service.mapper.PassengerMapper;
import com.example.user_service.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passengers")
@Tag(name = "Passengers", description = "Управление пассажирами")
public class PassengerController {

    private final PassengerService passengerService;
    private final PassengerMapper mapper;

    public PassengerController(PassengerService passengerService, PassengerMapper mapper) {
        this.passengerService = passengerService;
        this.mapper = mapper;
    }

    @Operation(summary = "Зарегистрировать пассажира")
    @PostMapping
    public ResponseEntity<PassengerResponse> register(@Valid @RequestBody PassengerRequest request) {
        Passenger passenger = passengerService.RegisterPassenger(request);
        PassengerResponse response = mapper.toResponse(passenger);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить профиль пассажира по ID")
    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> get(@PathVariable Long id) {
        try {
            Passenger passenger = passengerService.getPassengerById(id);
            PassengerResponse response = mapper.toResponse(passenger);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
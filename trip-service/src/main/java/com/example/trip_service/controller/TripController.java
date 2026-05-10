package com.example.trip_service.controller;

import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.dto.RatingRequest;
import com.example.trip_service.dto.TripResponse;
import com.example.trip_service.dto.UpdateTripStatusRequest;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.mapper.TripMapper;
import com.example.trip_service.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Управление поездками")
@SecurityRequirement(name = "Bearer Authentication")
public class TripController {

    private final TripService tripService;

    @Operation(summary = "Создать поездку")
    @PostMapping
    public ResponseEntity<TripResponse> create(
            @Valid @RequestBody CreateTripRequest request,
            @RequestHeader("X-User-Id") Long passengerId,
            @RequestHeader("X-User-Type") String userType) {

        log.info("Create trip - passengerId: {}, userType: {}", passengerId, userType);

        if (!"PASSENGER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Trip trip = tripService.create(request, passengerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(TripMapper.toResponse(trip));
        } catch (IllegalArgumentException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating trip: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получить поездку по ID")
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Type") String userType) {

        try {
            Trip trip = tripService.getById(id);

            if ("PASSENGER".equals(userType) && !trip.getPassengerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if ("DRIVER".equals(userType) && !trip.getDriverId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(TripMapper.toResponse(trip));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Оценить поездку (только для пассажира)")
    @PostMapping("/{id}/rating")
    public ResponseEntity<TripResponse> rate(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            @RequestHeader("X-User-Id") Long passengerId,
            @RequestHeader("X-User-Type") String userType) {

        if (!"PASSENGER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Trip trip = tripService.rateTrip(id, passengerId, request.getRating());
            return ResponseEntity.ok(TripMapper.toResponse(trip));
        } catch (IllegalArgumentException e) {
            log.error("Rating error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получить поездки пассажира")
    @GetMapping
    public ResponseEntity<List<TripResponse>> getByPassenger(
            @RequestHeader("X-User-Id") Long passengerId,
            @RequestHeader("X-User-Type") String userType) {

        if (!"PASSENGER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<TripResponse> trips = tripService.getByPassengerId(passengerId)
                    .stream()
                    .map(TripMapper::toResponse)
                    .toList();

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            log.error("Error fetching trips: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Обновить статус поездки")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripStatusRequest request,
            @RequestHeader("X-User-Id") Long driverId,
            @RequestHeader("X-User-Type") String userType) {

        if (!"DRIVER".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Trip trip = tripService.updateStatus(id, request.getStatus(), driverId);
            return ResponseEntity.ok(TripMapper.toResponse(trip));
        } catch (IllegalArgumentException e) {
            log.error("Forbidden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error updating trip: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
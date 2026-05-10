package com.example.notification_service.controller;

import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Управление уведомлениями")
public class NotificationController {

    private final NotificationRepository repository;

    @Operation(summary = "Получить уведомления по tripId")
    @GetMapping
    public ResponseEntity<List<NotificationTask>> getByTripId(
            @RequestParam(required = false) Long tripId) {

        if (tripId == null) {
            return ResponseEntity.ok(repository.findAll());
        }

        return ResponseEntity.ok(repository.findByTripId(tripId));
    }

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "service", "notification-service");
    }
}
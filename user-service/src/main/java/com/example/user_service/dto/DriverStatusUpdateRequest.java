package com.example.user_service.dto;

import com.example.user_service.entity.Driver.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverStatusUpdateRequest {

    @NotNull(message = "Статус обязателен")
    private DriverStatus status;
}
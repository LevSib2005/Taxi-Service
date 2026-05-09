package com.example.trip_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTripRequest {
    @NotBlank(message = "Адрес отправления обязателен")
    private String origin;

    @NotBlank(message = "Адрес назначения обязателен")
    private String destination;
}
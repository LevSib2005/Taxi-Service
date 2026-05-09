package com.example.trip_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTripRequest {
    @NotNull(message = "ID пассажира обязателен")
    private Long passengerId;

    @NotBlank(message = "Адрес отправления обязателен")
    private String origin;

    @NotBlank(message = "Адрес назначения обязателен")
    private String destination;
}
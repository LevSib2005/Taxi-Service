package com.example.trip_service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import com.example.trip_service.entity.Trip.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Информация о поездке")
public class TripResponse {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private TripStatus status;
    private String origin;
    private String destination;
    private Double price;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
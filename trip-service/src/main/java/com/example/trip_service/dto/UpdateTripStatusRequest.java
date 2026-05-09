package com.example.trip_service.dto;

import com.example.trip_service.entity.Trip.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTripStatusRequest {
    private TripStatus status;
}
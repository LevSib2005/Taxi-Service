package com.example.trip_service.mapper;

import com.example.trip_service.dto.TripResponse;
import com.example.trip_service.entity.Trip;

public class TripMapper {

    public static TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getPassengerId(),
                trip.getDriverId(),
                trip.getStatus(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getPrice(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
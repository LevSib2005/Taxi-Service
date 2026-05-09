package com.example.trip_service.mapper;

import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.dto.TripResponse;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.entity.Trip.TripStatus;

public class TripMapper {

    public static Trip toEntity(CreateTripRequest request) {
        Trip trip = new Trip();
        trip.setPassengerId(request.getPassengerId());
        trip.setOrigin(request.getOrigin());
        trip.setDestination(request.getDestination());
        trip.setStatus(TripStatus.CREATED);
        return trip;
    }

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

    public static void updateStatus(Trip trip, TripStatus newStatus) {
        trip.setStatus(newStatus);
    }
}
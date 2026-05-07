package com.example.user_service.mapper;

import com.example.user_service.dto.PassengerRequest;
import com.example.user_service.dto.PassengerResponse;
import com.example.user_service.entity.Passenger;
import org.springframework.stereotype.Component;

@Component
public class PassengerMapper {

    public Passenger toEntity(PassengerRequest request) {
        Passenger passenger = new Passenger();
        passenger.setName(request.getName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());
        return passenger;
    }

    public PassengerResponse toResponse(Passenger passenger) {
        PassengerResponse response = new PassengerResponse();
        response.setId(passenger.getId());
        response.setName(passenger.getName());
        response.setEmail(passenger.getEmail());
        response.setPhone(passenger.getPhone());
        response.setCreatedAt(passenger.getCreatedAt());
        return response;
    }
}
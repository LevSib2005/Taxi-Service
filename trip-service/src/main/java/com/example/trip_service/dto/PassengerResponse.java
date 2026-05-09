package com.example.trip_service.dto;

import lombok.Data;

@Data
public class PassengerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
}
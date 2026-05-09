package com.example.trip_service.dto;

import lombok.Data;

@Data
public class DriverResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String status;
}
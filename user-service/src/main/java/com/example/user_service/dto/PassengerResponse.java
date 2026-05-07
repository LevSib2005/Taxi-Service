package com.example.user_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PassengerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
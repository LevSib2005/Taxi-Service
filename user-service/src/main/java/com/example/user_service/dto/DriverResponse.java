package com.example.user_service.dto;

import com.example.user_service.entity.Driver.DriverStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DriverResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private DriverStatus status;
    private LocalDateTime createdAt;
}
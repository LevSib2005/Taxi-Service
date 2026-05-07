package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DriverRequest {

    @NotBlank(message = "Имя обязательно")
    private String name;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    @NotBlank(message = "Номер прав обязателен")
    private String licenseNumber;
}
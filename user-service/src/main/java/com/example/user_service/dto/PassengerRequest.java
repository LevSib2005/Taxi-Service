package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PassengerRequest {

    @NotBlank(message = "Заполни поле")
    private String name;

    @Email
    @NotBlank(message = "Заполни поле")
    private String email;

    @NotBlank(message = "Заполни поле")
    private String phone;
}

package com.example.notification_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Notification Service API",
                version = "v1",
                description = "API для управления уведомлениями"
        )
)
public class OpenApiConfig {
}
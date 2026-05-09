package com.example.trip_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Trip Service API",
                version = "v1",
                description = "API для управления поездками"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        // ✅ Gateway - ПЕРВЫЙ в списке (по умолчанию)
                        new Server()
                                .url("http://localhost:8000")
                                .description("API Gateway (использовать этот!)"),
                        // Прямой доступ только для отладки
                        new Server()
                                .url("http://localhost:8082")
                                .description("Trip Service (прямой доступ - без заголовков!)")
                ));
    }
}
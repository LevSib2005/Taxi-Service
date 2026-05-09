package com.example.trip_service.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    // Список заголовков которые нужно скрыть
    private static final List<String> HIDDEN_HEADERS = List.of(
            "X-User-Id",
            "X-User-Type",
            "X-Gateway-Header"
    );

    @Bean
    public OperationCustomizer hideInternalHeaders() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getParameters() != null) {
                // Фильтруем — убираем внутренние заголовки
                List<Parameter> filtered = operation.getParameters()
                        .stream()
                        .filter(param -> !(
                                "header".equals(param.getIn()) &&
                                        HIDDEN_HEADERS.contains(param.getName())
                        ))
                        .collect(Collectors.toList());

                operation.setParameters(filtered);
            }
            return operation;
        };
    }
}
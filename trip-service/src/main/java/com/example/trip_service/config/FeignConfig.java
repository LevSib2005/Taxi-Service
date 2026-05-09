package com.example.trip_service.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${gateway.header}")
    private String gatewayHeader;

    @Value("${gateway.header-key}")
    private String headerKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Добавляем служебный заголовок gateway ко всем запросам
            requestTemplate.header(gatewayHeader, headerKey);
            log.debug("Feign request - adding header: {}={}", gatewayHeader, headerKey);
        };
    }
}
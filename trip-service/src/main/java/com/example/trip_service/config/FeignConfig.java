package com.example.trip_service.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${gateway.header-key}")
    private String headerKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Gateway-Header", headerKey);

            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String auth = attrs.getRequest().getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    requestTemplate.header("Authorization", auth);
                    log.debug("Feign: forwarded Authorization header");
                }
            }

            log.debug("Feign: X-Gateway-Header={}", headerKey);
        };
    }
}
package com.example.gateway_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class JwtGatewayProperties {
    private String secret;
    private String issuer;
    private String header;
    private String headerKey;
    private List<String> openRoutes = new ArrayList<>();
}
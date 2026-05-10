package com.example.gateway_service.filter;

import com.example.gateway_service.config.JwtGatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtGatewayProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            exchange = exchange.mutate().request(r -> r
                    .header(properties.getHeader(), properties.getHeaderKey())
            ).build();
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(
                    properties.getSecret().getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String userType = claims.get("userType", String.class);

            log.debug("Authenticated user: userId={}, userType={}", userId, userType);

            exchange = exchange.mutate().request(r -> r
                    .header("X-User-Id", userId)
                    .header("X-User-Type", userType)
                    .header(properties.getHeader(), properties.getHeaderKey())
            ).build();

            return chain.filter(exchange);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        if (path.equals("/passengers") || path.equals("/drivers")) {
            return true;
        }
        if (path.startsWith("/auth/")) {
            return true;
        }
        if (path.contains("/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/webjars")) {
            return true;
        }
        if (properties.getOpenRoutes() != null) {
            return properties.getOpenRoutes().stream()
                    .anyMatch(path::startsWith);
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
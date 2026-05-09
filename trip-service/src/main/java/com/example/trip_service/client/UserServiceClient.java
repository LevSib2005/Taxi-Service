package com.example.trip_service.client;

import com.example.trip_service.dto.DriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${gateway.header}")
    private String gatewayHeader;

    @Value("${gateway.header-key}")
    private String headerKey;

    public void checkPassenger(Long passengerId) {
        try {
            restTemplate.exchange(
                    userServiceUrl + "/passengers/" + passengerId,
                    HttpMethod.GET,
                    buildRequest(),
                    Object.class
            );
            log.debug("Passenger verified: id={}", passengerId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Пассажир не найден: " + passengerId);
        }
    }

    public DriverResponse getAvailableDriver() {
        try {
            DriverResponse driver = restTemplate.exchange(
                    userServiceUrl + "/drivers/available",
                    HttpMethod.GET,
                    buildRequest(),
                    DriverResponse.class
            ).getBody();

            if (driver == null) {
                throw new IllegalStateException("Нет доступных водителей");
            }

            log.debug("Available driver fetched: id={}", driver.getId());
            return driver;
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalStateException("Нет доступных водителей");
        }
    }

    // Добавляем служебный заголовок gateway
    private HttpEntity<Void> buildRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(gatewayHeader, headerKey);
        return new HttpEntity<>(headers);
    }
}
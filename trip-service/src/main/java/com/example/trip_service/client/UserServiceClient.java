package com.example.trip_service.client;

import com.example.trip_service.config.FeignConfig;
import com.example.trip_service.dto.DriverResponse;
import com.example.trip_service.dto.PassengerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/passengers/{id}")
    PassengerResponse getPassenger(@PathVariable("id") Long id);

    @GetMapping("/drivers/available")
    DriverResponse getAvailableDriver();
}
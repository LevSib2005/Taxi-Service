package com.example.user_service.mapper;

import com.example.user_service.dto.DriverRequest;
import com.example.user_service.dto.DriverResponse;
import com.example.user_service.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

    public Driver toEntity(DriverRequest request) {
        Driver driver = new Driver();
        driver.setName(request.getName());
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        return driver;
    }

    public DriverResponse toResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setName(driver.getName());
        response.setEmail(driver.getEmail());
        response.setPhone(driver.getPhone());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setStatus(driver.getStatus());
        response.setCreatedAt(driver.getCreatedAt());
        return response;
    }
}
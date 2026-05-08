package com.example.user_service.service;

import com.example.user_service.dto.DriverRequest;
import com.example.user_service.entity.Driver;
import com.example.user_service.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DriverService {
    private final DriverRepository driverRepository;
    public DriverService(DriverRepository driverRepository){
        this.driverRepository = driverRepository;
    }
    @Transactional
    public Driver registerDriver(DriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Водитель с таким email уже существует");
        }
        Driver driver = new Driver();
        driver.setName(request.getName());
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        return driverRepository.save(driver);
    }

    public Driver getDriverById(Long id){
        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Водитель не найден"));
    }

    @Transactional
    public Driver updateDriverStatus(Long id, Driver.DriverStatus newStatus){
        Driver driver = getDriverById(id);
        driver.setStatus(newStatus);
        return driverRepository.save(driver);
    }
}

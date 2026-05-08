package com.example.user_service.service;

import com.example.user_service.dto.DriverRequest;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.Driver;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.repository.DriverRepository;
import com.example.user_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

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

    public TokenResponse generateTokensForDriver(Long driverId) {
        String accessToken = jwtTokenProvider.generateAccessToken(driverId, UserType.DRIVER.name());
        String refreshToken = refreshTokenService.createRefreshToken(driverId, UserType.DRIVER, 7 * 24 * 3600_000L);
        return new TokenResponse(accessToken, refreshToken);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Водитель не найден"));
    }

    public Driver getDriverByEmail(String email) {
        return driverRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Водитель не найден"));
    }

    @Transactional
    public Driver updateDriverStatus(Long id, Driver.DriverStatus newStatus) {
        Driver driver = getDriverById(id);
        driver.setStatus(newStatus);
        return driverRepository.save(driver);
    }
}
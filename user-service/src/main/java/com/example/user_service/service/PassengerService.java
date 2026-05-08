package com.example.user_service.service;

import com.example.user_service.dto.PassengerRequest;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.Passenger;
import com.example.user_service.entity.RefreshToken.UserType;
import com.example.user_service.repository.PassengerRepository;
import com.example.user_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public Passenger registerPassenger(PassengerRequest request) {
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пассажир с таким email уже существует");
        }

        Passenger passenger = new Passenger();
        passenger.setName(request.getName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());

        return passengerRepository.save(passenger);
    }

    public TokenResponse generateTokensForPassenger(Long passengerId) {
        String accessToken = jwtTokenProvider.generateAccessToken(passengerId, UserType.PASSENGER.name());
        String refreshToken = refreshTokenService.createRefreshToken(passengerId, UserType.PASSENGER, 7 * 24 * 3600_000L);
        return new TokenResponse(accessToken, refreshToken);
    }

    public Passenger getPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));
    }

    public Passenger getPassengerByEmail(String email) {
        return passengerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));
    }
}
package com.example.user_service.service;

import com.example.user_service.dto.PassengerRequest;
import com.example.user_service.entity.Passenger;
import com.example.user_service.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PassengerService {
    private final PassengerRepository passengerRepository;
    public PassengerService(PassengerRepository passengerRepository){
        this.passengerRepository = passengerRepository;
    }
    @Transactional
    public Passenger RegisterPassenger(PassengerRequest request) {
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пассажир с таким email уже существует");
        }
        Passenger passenger = new Passenger();
        passenger.setName(request.getName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());
        // createdAt автоматом
        return passengerRepository.save(passenger);
    }
    public Passenger getPassengerById(Long id){
        return passengerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));
    }
}

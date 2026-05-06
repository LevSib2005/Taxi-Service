package com.example.user_service.service;

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
    public Passenger RegisterPassenger(String name, String email, String phone){
        if(passengerRepository.existsByEmail(email)){
            throw new IllegalArgumentException("такой Email уже существует");
        }
        Passenger passenger = new Passenger();
        passenger.setName(name);
        passenger.setEmail(email);
        passenger.setPhone(phone);
        return passengerRepository.save(passenger);
    }
    public Passenger getPassengerById(Long id){
        return passengerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));
    }
}

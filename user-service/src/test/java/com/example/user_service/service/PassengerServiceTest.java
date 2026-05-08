package com.example.user_service.service;

import com.example.user_service.entity.Passenger;
import com.example.user_service.repository.*;
import com.example.user_service.repository.PassengerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    PassengerRepository repository;

    @InjectMocks
    PassengerService service;

    @Test
    void getById_shouldReturnPassenger() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(passenger));

        Passenger result = service.getPassengerById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(repository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getPassengerById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пассажир не найден");
    }
}

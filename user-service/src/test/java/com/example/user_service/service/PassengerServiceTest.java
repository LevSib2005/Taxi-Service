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
    void register_shouldSaveAndReturnPassenger() {
        // given
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("Ivan");
        passenger.setEmail("ivan@mail.ru");
        passenger.setPhone("+7999");
        when(repository.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(repository.save(any(Passenger.class))).thenReturn(passenger);

        // when
        Passenger result = service.RegisterPassenger("Ivan", "ivan@mail.ru", "+7999");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void register_shouldThrowIfEmailExists() {
        when(repository.existsByEmail("ivan@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> service.RegisterPassenger("Ivan", "ivan@mail.ru", "+7999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("такой Email уже существует");
    }

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

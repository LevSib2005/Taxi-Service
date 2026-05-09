package com.example.trip_service.service;

import com.example.trip_service.client.UserServiceClient;
import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.dto.DriverResponse;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.entity.Trip.TripStatus;
import com.example.trip_service.repository.TripRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserServiceClient userServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TRIP_EXCHANGE = "trip.exchange";
    private static final String USER_EXCHANGE = "user.exchange";
    private static final String DRIVER_CACHE_KEY = "available_driver";
    private static final long CACHE_TTL_SECONDS = 60;

    @Transactional
    public Trip create(CreateTripRequest request, Long passengerId) {
        log.info("Creating trip - passengerId: {}, origin: {}, destination: {}",
                passengerId, request.getOrigin(), request.getDestination());

        try {
            // Проверяем что пассажир существует
            log.debug("Checking passenger existence...");
            checkPassenger(passengerId);
            log.debug("Passenger check passed");

            // Получаем свободного водителя
            log.debug("Fetching available driver...");
            DriverResponse driver = getAvailableDriver();
            log.info("Driver assigned: id={}, name={}", driver.getId(), driver.getName());

            Trip trip = new Trip();
            trip.setPassengerId(passengerId);
            trip.setDriverId(driver.getId());
            trip.setStatus(TripStatus.CREATED);
            trip.setOrigin(request.getOrigin());
            trip.setDestination(request.getDestination());
            trip.setPrice(calculatePrice());

            tripRepository.save(trip);

            log.info("Trip created successfully: id={}", trip.getId());

            // Уведомляем о создании поездки
            rabbitTemplate.convertAndSend(
                    TRIP_EXCHANGE,
                    "trip.created",
                    trip.getId()
            );

            // Меняем статус водителя на BUSY
            rabbitTemplate.convertAndSend(
                    USER_EXCHANGE,
                    "driver.status.update",
                    new DriverStatusEvent(driver.getId(), "BUSY")
            );

            // Кэшируем водителя
            redisTemplate.opsForValue().set(DRIVER_CACHE_KEY, driver, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

            return trip;
        } catch (FeignException.NotFound e) {
            log.error("Passenger or driver not found: {}", e.getMessage());
            throw new IllegalArgumentException("Пассажир не найден или нет доступных водителей");
        } catch (FeignException.Forbidden e) {
            log.error("Forbidden access to user-service: {}", e.getMessage());
            throw new IllegalStateException("Ошибка доступа к сервису пользователей");
        } catch (FeignException e) {
            log.error("Feign error: status={}, message={}", e.status(), e.getMessage());
            throw new RuntimeException("Ошибка связи с сервисом пользователей", e);
        } catch (Exception e) {
            log.error("Error creating trip: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Trip getById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поездка не найдена: " + id));
    }

    @Transactional(readOnly = true)
    public List<Trip> getByPassengerId(Long passengerId) {
        return tripRepository.findByPassengerId(passengerId);
    }

    @Transactional
    public Trip updateStatus(Long id, TripStatus newStatus, Long driverId) {
        Trip trip = getById(id);

        if (!trip.getDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Водитель не назначен на эту поездку");
        }

        trip.setStatus(newStatus);
        tripRepository.save(trip);

        log.debug("Trip status updated: id={}, status={}", id, newStatus);

        rabbitTemplate.convertAndSend(
                TRIP_EXCHANGE,
                "trip.status.changed",
                trip.getId()
        );

        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            rabbitTemplate.convertAndSend(
                    USER_EXCHANGE,
                    "driver.status.update",
                    new DriverStatusEvent(trip.getDriverId(), "FREE")
            );
            redisTemplate.delete(DRIVER_CACHE_KEY);
        }

        return trip;
    }

    private void checkPassenger(Long passengerId) {
        try {
            userServiceClient.getPassenger(passengerId);
            log.debug("Passenger verified: id={}", passengerId);
        } catch (FeignException.NotFound e) {
            log.error("Passenger not found: {}", passengerId);
            throw new IllegalArgumentException("Пассажир не найден: " + passengerId);
        }
    }

    private DriverResponse getAvailableDriver() {
        // Сначала проверяем кэш
        DriverResponse cached = (DriverResponse) redisTemplate.opsForValue().get(DRIVER_CACHE_KEY);
        if (cached != null) {
            log.debug("Driver found in cache: id={}", cached.getId());
            return cached;
        }

        // Если нет в кэше — запрашиваем через Feign
        try {
            DriverResponse driver = userServiceClient.getAvailableDriver();
            redisTemplate.opsForValue().set(DRIVER_CACHE_KEY, driver, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("Driver fetched from user-service: id={}", driver.getId());
            return driver;
        } catch (FeignException.NotFound e) {
            log.error("No available drivers found");
            throw new IllegalStateException("Нет доступных водителей");
        } catch (FeignException.ServiceUnavailable e) {
            log.error("User service unavailable");
            throw new IllegalStateException("Сервис пользователей недоступен");
        }
    }

    private double calculatePrice() {
        double distance = 10.0;
        double rate = 2.5;
        return distance * rate;
    }

    public record DriverStatusEvent(Long driverId, String status) {}
}
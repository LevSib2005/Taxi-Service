package com.example.trip_service.service;

import com.example.trip_service.client.UserServiceClient;
import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.dto.DriverResponse;
import com.example.trip_service.dto.TripEvent; // ← добавить DTO
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
            checkPassenger(passengerId);
            DriverResponse driver = getAvailableDriver();

            Trip trip = new Trip();
            trip.setPassengerId(passengerId);
            trip.setDriverId(driver.getId());
            trip.setStatus(TripStatus.CREATED);
            trip.setOrigin(request.getOrigin());
            trip.setDestination(request.getDestination());
            trip.setPrice(calculatePrice());

            tripRepository.save(trip);
            log.info("Trip created successfully: id={}", trip.getId());

            publishTripEvent(trip, "trip.created");

            rabbitTemplate.convertAndSend(
                    USER_EXCHANGE,
                    "driver.status.update",
                    new DriverStatusEvent(driver.getId(), "BUSY")
            );

            redisTemplate.opsForValue().set(DRIVER_CACHE_KEY, driver, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

            return trip;
        } catch (FeignException.NotFound e) {
            log.error("Passenger or driver not found: {}", e.getMessage());
            throw new IllegalArgumentException("Пассажир не найден или нет доступных водителей");
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

        publishTripEvent(trip, "trip.status.changed");

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

    @Transactional
    public Trip rateTrip(Long tripId, Long passengerId, Integer rating) {
        Trip trip = getById(tripId);

        if (!trip.getPassengerId().equals(passengerId)) {
            throw new IllegalArgumentException("Вы не являетесь пассажиром этой поездки");
        }

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new IllegalArgumentException("Оценить можно только завершённую поездку");
        }

        if (trip.getRating() != null) {
            throw new IllegalArgumentException("Поездка уже оценена");
        }

        trip.setRating(rating);
        return tripRepository.save(trip);
    }

    private void publishTripEvent(Trip trip, String routingKey) {
        TripEvent event = new TripEvent();
        event.setTripId(trip.getId());
        event.setPassengerId(trip.getPassengerId());
        event.setDriverId(trip.getDriverId());
        event.setOrigin(trip.getOrigin());
        event.setDestination(trip.getDestination());
        event.setPrice(trip.getPrice());
        event.setStatus(trip.getStatus().name());

        rabbitTemplate.convertAndSend(TRIP_EXCHANGE, routingKey, event);
        log.debug("Published TripEvent: routingKey={}, tripId={}", routingKey, trip.getId());
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
        try {
            DriverResponse driver = userServiceClient.getAvailableDriver();
            log.debug("Driver assigned via user-service: id={}", driver.getId());
            return driver;
        } catch (FeignException.NotFound e) {
            log.error("No available drivers found");
            throw new IllegalStateException("Нет доступных водителей");
        }
    }

    private double calculatePrice() {
        double distance = 10.0;
        double rate = 2.5;
        return distance * rate;
    }

    public record DriverStatusEvent(Long driverId, String status) {}
}
package com.example.trip_service.service;

import com.example.trip_service.client.UserServiceClient;
import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.dto.DriverResponse;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.entity.Trip.TripStatus;
import com.example.trip_service.repository.TripRepository;
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
        // Проверяем что пассажир существует
        userServiceClient.checkPassenger(passengerId);

        // Получаем свободного водителя
        DriverResponse driver = getAvailableDriver();

        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setDriverId(driver.getId());
        trip.setStatus(TripStatus.CREATED);
        trip.setOrigin(request.getOrigin());
        trip.setDestination(request.getDestination());
        trip.setPrice(calculatePrice());

        tripRepository.save(trip);

        log.debug("Trip created: id={}, passengerId={}, driverId={}",
                trip.getId(), passengerId, driver.getId());

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

        // Только водитель назначенный на поездку может менять статус
        if (!trip.getDriverId().equals(driverId)) {
            throw new IllegalArgumentException("Водитель не назначен на эту поездку");
        }

        trip.setStatus(newStatus);
        tripRepository.save(trip);

        log.debug("Trip status updated: id={}, status={}", id, newStatus);

        // Уведомляем об изменении статуса
        rabbitTemplate.convertAndSend(
                TRIP_EXCHANGE,
                "trip.status.changed",
                trip.getId()
        );

        // Если поездка завершена или отменена — освобождаем водителя
        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            rabbitTemplate.convertAndSend(
                    USER_EXCHANGE,
                    "driver.status.update",
                    new DriverStatusEvent(trip.getDriverId(), "FREE")
            );
            // Очищаем кэш водителя
            redisTemplate.delete(DRIVER_CACHE_KEY);
        }

        return trip;
    }

    private DriverResponse getAvailableDriver() {
        // Сначала проверяем кэш
        DriverResponse cached = (DriverResponse) redisTemplate.opsForValue().get(DRIVER_CACHE_KEY);
        if (cached != null) {
            log.debug("Driver found in cache: id={}", cached.getId());
            return cached;
        }

        // Если нет в кэше — запрашиваем из user-service
        DriverResponse driver = userServiceClient.getAvailableDriver();
        redisTemplate.opsForValue().set(DRIVER_CACHE_KEY, driver, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        return driver;
    }

    private double calculatePrice() {
        double distance = 10.0;
        double rate = 2.5;
        return distance * rate;
    }

    // Внутренний record для события
    public record DriverStatusEvent(Long driverId, String status) {}
}
package com.example.notification_service.service;

import com.example.notification_service.dto.TripEvent;
import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.entity.NotificationTask.TaskStatus;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void createNotifications(TripEvent event) {
        // Уведомление для пассажира
        NotificationTask passengerTask = new NotificationTask();
        passengerTask.setTripId(event.getTripId());
        passengerTask.setRecipientType("PASSENGER");
        passengerTask.setRecipientId(event.getPassengerId());
        passengerTask.setMessage(buildMessage(event, "PASSENGER"));
        passengerTask.setStatus(TaskStatus.PENDING);
        passengerTask.setAttempts(0);
        repository.save(passengerTask);

        // Уведомление для водителя
        if (event.getDriverId() != null) {
            NotificationTask driverTask = new NotificationTask();
            driverTask.setTripId(event.getTripId());
            driverTask.setRecipientType("DRIVER");
            driverTask.setRecipientId(event.getDriverId());
            driverTask.setMessage(buildMessage(event, "DRIVER"));
            driverTask.setStatus(TaskStatus.PENDING);
            driverTask.setAttempts(0);
            repository.save(driverTask);
        }

        log.debug("Created notifications for tripId={}", event.getTripId());
    }

    private String buildMessage(TripEvent event, String role) {
        return String.format(
                "[%s] Trip #%d: %s | %s → %s | Price: ₽%.2f",
                role,
                event.getTripId(),
                event.getStatus(),
                event.getOrigin(),
                event.getDestination(),
                event.getPrice()
        );
    }
}
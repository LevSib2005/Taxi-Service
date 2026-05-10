package com.example.notification_service.listener;

import com.example.notification_service.dto.TripEvent;
import com.example.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripEventListener {

    private final NotificationService notificationService;

    @RabbitListener(
            queues = "notification.queue",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleTripEvent(TripEvent event) {
        log.info("📩 Received TripEvent: tripId={}, status={}, passenger={}, driver={}",
                event.getTripId(), event.getStatus(), event.getPassengerId(), event.getDriverId());

        notificationService.createNotifications(event);

        log.info("✅ Notifications created for tripId={}", event.getTripId());
    }
}
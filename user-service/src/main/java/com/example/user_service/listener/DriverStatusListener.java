package com.example.user_service.listener;

import com.example.user_service.entity.Driver;
import com.example.user_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverStatusListener {

    private final DriverRepository driverRepository;

    @RabbitListener(queues = "driver.status.queue")
    public void handleDriverStatusUpdate(DriverStatusEvent event) {
        log.info("📩 Received DriverStatusEvent: driverId={}, status={}",
                event.driverId(), event.status());

        updateDriverStatus(event.driverId(), event.status());
    }

    @Transactional
    protected void updateDriverStatus(Long driverId, String status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        Driver.DriverStatus newStatus;
        try {
            newStatus = Driver.DriverStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid driver status: {}", status);
            return;
        }

        driver.setStatus(newStatus);
        driverRepository.save(driver);

        log.info("Driver status updated: id={}, status={}", driverId, newStatus);
    }

    public record DriverStatusEvent(Long driverId, String status) {}
}
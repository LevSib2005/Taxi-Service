package com.example.notification_service.worker;

import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.entity.NotificationTask.TaskStatus;
import com.example.notification_service.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final NotificationRepository repository;
    private final PlatformTransactionManager transactionManager;

    @Value("${notification.worker.pool-size:4}")
    private int poolSize;

    @Value("${notification.worker.poll-interval-ms:2000}")
    private long pollIntervalMs;

    @Value("${notification.worker.max-retry-attempts:3}")
    private int maxRetryAttempts;

    private ExecutorService executor;

    @PostConstruct
    public void start() {
        log.info("🚀 Starting NotificationWorker with {} threads", poolSize);
        executor = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < poolSize; i++) {
            final int workerId = i + 1;
            executor.submit(() -> process(workerId));
        }
    }

    private void process(int workerId) {
        log.info("Worker-{} started", workerId);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Получаем задачи в транзакции
                List<NotificationTask> tasks = transactionTemplate.execute(status ->
                        repository.findPendingTasksWithLock(TaskStatus.PENDING)
                );

                if (tasks != null && !tasks.isEmpty()) {
                    for (NotificationTask task : tasks) {
                        processTask(task, workerId, transactionTemplate);
                    }
                }

                Thread.sleep(pollIntervalMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Worker-{} interrupted", workerId);
                break;
            } catch (Exception e) {
                log.error("Worker-{} error: {}", workerId, e.getMessage(), e);
            }
        }

        log.info("Worker-{} stopped", workerId);
    }

    protected void processTask(NotificationTask task, int workerId, TransactionTemplate transactionTemplate) {
        transactionTemplate.executeWithoutResult(status -> {
            try {
                log.debug("Worker-{} processing task id={}", workerId, task.getId());

                // Обновляем статус на PROCESSING
                task.setStatus(TaskStatus.PROCESSING);
                repository.save(task);

                // Отправка уведомления
                sendNotification(task);

                task.setStatus(TaskStatus.SENT);
                log.info("✅ Worker-{} sent notification: {}", workerId, task.getMessage());

            } catch (Exception e) {
                task.setAttempts(task.getAttempts() + 1);
                log.error("❌ Worker-{} failed to send (attempt {}): {}",
                        workerId, task.getAttempts(), e.getMessage());

                if (task.getAttempts() >= maxRetryAttempts) {
                    task.setStatus(TaskStatus.FAILED);
                    log.error("💀 Task id={} marked as FAILED after {} attempts",
                            task.getId(), maxRetryAttempts);
                } else {
                    task.setStatus(TaskStatus.PENDING);
                }
            }

            repository.save(task);
        });
    }

    private void sendNotification(NotificationTask task) throws InterruptedException {
        log.info("📨 Sending: {}", task.getMessage());
        Thread.sleep(1000);

        // Для тестирования ошибок:
        // if (Math.random() < 0.3) throw new RuntimeException("Simulated failure");
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down NotificationWorker...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        log.info("✅ NotificationWorker stopped");
    }
}
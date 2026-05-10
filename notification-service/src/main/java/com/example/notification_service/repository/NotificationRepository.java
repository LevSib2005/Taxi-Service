package com.example.notification_service.repository;

import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.entity.NotificationTask.TaskStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByTripId(Long tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NotificationTask n WHERE n.status = :status ORDER BY n.createdAt ASC")
    List<NotificationTask> findPendingTasksWithLock(TaskStatus status);
}
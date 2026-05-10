package com.example.notification_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_tasks", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_trip_id", columnList = "trip_id")
})
@Data
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "recipient_type", nullable = false)
    private String recipientType;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(nullable = false)
    private Integer attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TaskStatus {
        PENDING,
        PROCESSING,
        SENT,
        FAILED
    }
}
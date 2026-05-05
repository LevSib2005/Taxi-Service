package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.FREE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum DriverStatus{
        FREE, BUSY, OFFLINE
    }
}

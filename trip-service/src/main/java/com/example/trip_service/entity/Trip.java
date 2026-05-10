package com.example.trip_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long passengerId;
    private Long driverId;

    @Enumerated(EnumType.STRING)
    private TripStatus status = TripStatus.CREATED;

    private String origin;
    private String destination;
    private Double price;

    private Integer rating;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TripStatus {
        CREATED,
        ACCEPTED,
        STARTED,
        COMPLETED,
        CANCELLED
    }
}

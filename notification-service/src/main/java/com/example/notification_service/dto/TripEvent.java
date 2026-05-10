package com.example.notification_service.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TripEvent implements Serializable {

    private Long tripId;
    private Long passengerId;
    private Long driverId;

    private String origin;
    private String destination;

    private Double distanceKm;
    private Double durationMin;

    private Integer rating;

    private Double price;
    private String status;
}
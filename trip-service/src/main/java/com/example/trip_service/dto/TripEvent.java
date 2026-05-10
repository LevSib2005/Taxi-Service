package com.example.trip_service.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TripEvent implements Serializable {
    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private String origin;
    private String destination;
    private Double price;
    private String status;
}
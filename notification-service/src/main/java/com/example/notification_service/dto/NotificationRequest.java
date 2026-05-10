package com.example.notification_service.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long tripId;
    private String recipientType;
    private Long recipientId;
    private String message;
}
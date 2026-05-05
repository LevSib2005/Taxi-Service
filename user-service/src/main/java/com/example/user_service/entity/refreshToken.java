package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.catalina.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "refresh_token")
public class refreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true)
    private String tokenHash;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum UserType{
        PASSENGER, DRIVER
    }
}

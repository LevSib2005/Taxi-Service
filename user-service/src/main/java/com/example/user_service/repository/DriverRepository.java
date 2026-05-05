package com.example.user_service.repository;

import com.example.user_service.entity.Driver;
import com.example.user_service.entity.Driver.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatus(DriverStatus status);
}

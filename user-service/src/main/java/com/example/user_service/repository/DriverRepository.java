package com.example.user_service.repository;

import com.example.user_service.entity.Driver;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.status = 'FREE'")
    Optional<Driver> findFirstFreeAndLock();

    Optional<Driver> findByEmail(String email);
    Optional<Driver> findFirstByStatus(Driver.DriverStatus status);
}
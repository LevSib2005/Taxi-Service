package com.example.user_service.repository;

import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.RefreshToken.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUserIdAndUserType(Long userId, UserType userType);
}

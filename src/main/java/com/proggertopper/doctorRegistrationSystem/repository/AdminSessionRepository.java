package com.proggertopper.doctorRegistrationSystem.repository;

import com.proggertopper.doctorRegistrationSystem.entity.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminSessionRepository extends JpaRepository<AdminSession, Long> {

    Optional<AdminSession> findByTokenAndExpiresAtAfter(
            String token,
            LocalDateTime now
    );

    void deleteByToken(String token);
}

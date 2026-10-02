package com.proggertopper.doctorRegistrationSystem.repository;

import com.proggertopper.doctorRegistrationSystem.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneAndCodeAndUsedFalseOrderByCreatedAtDesc(
            String phone,
            String code
    );

    boolean existsByPhoneAndCreatedAtAfter(
            String phone,
            LocalDateTime createdAt
    );

    long countByPhoneAndCreatedAtAfter(
            String phone,
            LocalDateTime createdAt
    );
}

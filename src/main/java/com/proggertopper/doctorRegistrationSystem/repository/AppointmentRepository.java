package com.proggertopper.doctorRegistrationSystem.repository;

import com.proggertopper.doctorRegistrationSystem.entity.Appointment;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByCancelTokenAndStatus(
            String cancelToken,
            AppointmentStatus status
    );

    Optional<Appointment> findBySlotIdAndStatus(
            Long slotId,
            AppointmentStatus status
    );

    List<Appointment> findBySlotSlotDateOrderBySlotStartTime(
            LocalDate slotDate
    );

    boolean existsByClientPhoneNormalizedAndStatus(
            String clientPhoneNormalized,
            AppointmentStatus status
    );

    Optional<Appointment> findByIdAndStatus(
            Long id,
            AppointmentStatus status
    );
}

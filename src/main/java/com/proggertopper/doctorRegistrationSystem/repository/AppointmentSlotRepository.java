package com.proggertopper.doctorRegistrationSystem.repository;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentSlot;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import com.proggertopper.doctorRegistrationSystem.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findBySlotDateAndTypeOrderByStartTime(
            LocalDate slotDate,
            AppointmentType type
    );

    boolean existsBySlotDateAndStartTimeAndType(
            LocalDate slotDate,
            LocalTime startTime,
            AppointmentType type
    );

    @Modifying
    @Query("""
        DELETE FROM AppointmentSlot s
        WHERE s.id = :slotId
          AND s.status = :freeStatus
        """)
    int deleteFreeSlot(Long slotId, SlotStatus freeStatus);

    @Modifying
    @Query("""
        UPDATE AppointmentSlot s
        SET s.status = :freeStatus
        WHERE s.id = :slotId
          AND s.status = :bookedStatus
        """)
    int cancelBooking(
            Long slotId,
            SlotStatus bookedStatus,
            SlotStatus freeStatus
    );

    @Modifying
    @Query("""
UPDATE AppointmentSlot s
SET s.status = :bookedStatus
WHERE s.id = :slotId
  AND s.status = :freeStatus
""")
    int bookFreeSlot(
            Long slotId,
            SlotStatus freeStatus,
            SlotStatus bookedStatus
    );
}

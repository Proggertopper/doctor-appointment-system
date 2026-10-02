package com.proggertopper.doctorRegistrationSystem.dto;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import com.proggertopper.doctorRegistrationSystem.entity.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentSlotResponse(
        Long id,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentType type,
        SlotStatus status
) {
}

package com.proggertopper.doctorRegistrationSystem.dto;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentStatus;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorAppointmentResponse(
        Long appointmentId,
        Long slotId,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentType type,
        String clientName,
        String clientPhone,
        String comment,
        AppointmentStatus status
) {
}

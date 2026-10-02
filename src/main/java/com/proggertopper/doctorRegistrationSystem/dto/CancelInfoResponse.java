package com.proggertopper.doctorRegistrationSystem.dto;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;

import java.time.LocalDate;
import java.time.LocalTime;

public record CancelInfoResponse(
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentType type
) {
}

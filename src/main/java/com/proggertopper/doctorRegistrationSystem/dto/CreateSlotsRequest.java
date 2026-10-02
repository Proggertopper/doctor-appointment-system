package com.proggertopper.doctorRegistrationSystem.dto;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSlotsRequest(

        @NotNull
        @FutureOrPresent
        LocalDate slotDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @Min(5)
        @Max(240)
        int durationMinutes,

        @NotNull
        AppointmentType type
) {
}

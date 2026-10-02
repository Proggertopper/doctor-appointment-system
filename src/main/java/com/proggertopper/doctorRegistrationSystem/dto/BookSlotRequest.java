package com.proggertopper.doctorRegistrationSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookSlotRequest(

        @NotBlank
        @Size(max = 100)
        String clientName,

        @NotBlank
        @Size(max = 30)
        String clientPhone,

        @Size(max = 1000)
        String comment
) {
}

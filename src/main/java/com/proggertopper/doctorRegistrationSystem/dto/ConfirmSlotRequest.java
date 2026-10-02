package com.proggertopper.doctorRegistrationSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmSlotRequest(

        @NotBlank
        @Size(max = 100)
        String clientName,

        @NotBlank
        @Size(max = 30)
        String clientPhone,

        @NotBlank
        String verificationCode,

        @Size(max = 1000)
        String comment
) {
}

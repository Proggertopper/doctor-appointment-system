package com.proggertopper.doctorRegistrationSystem.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank
        String phone,

        @NotBlank
        String code
) {
}

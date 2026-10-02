package com.proggertopper.doctorRegistrationSystem.dto;

import jakarta.validation.constraints.NotBlank;

public record SendOtpRequest(
        @NotBlank
        String phone
) {
}

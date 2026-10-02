package com.proggertopper.doctorRegistrationSystem.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelBookingRequest(
        @NotBlank String cancelToken
) {
}

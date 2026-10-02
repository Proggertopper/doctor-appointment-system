package com.proggertopper.doctorRegistrationSystem.dto;

public record TurboSmsResponse(
        Integer response_code,
        String response_status,
        Object response_result
) {
}

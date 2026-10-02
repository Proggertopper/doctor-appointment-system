package com.proggertopper.doctorRegistrationSystem.dto;

import java.time.LocalDateTime;

public record HoldSlotResponse(
        Long slotId,
        String holdToken,
        LocalDateTime heldUntil
) {
}

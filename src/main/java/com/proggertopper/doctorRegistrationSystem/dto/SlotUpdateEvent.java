package com.proggertopper.doctorRegistrationSystem.dto;

import com.proggertopper.doctorRegistrationSystem.entity.SlotStatus;

public record SlotUpdateEvent(
        Long slotId,
        SlotStatus status
) {
}

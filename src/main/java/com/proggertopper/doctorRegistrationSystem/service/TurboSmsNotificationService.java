package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TurboSmsNotificationService implements NotificationService {

    private final TurboSmsProvider turboSmsProvider;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendAppointmentConfirmation(Appointment appointment) {
        String cancelLink = baseUrl + "/cancel.html?token=" + appointment.getCancelToken();

        String text = """
                Ви записані до лікаря.
                Дата: %s
                Час: %s-%s
                Формат: %s
                Скасувати: %s
                """.formatted(
                appointment.getSlot().getSlotDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                appointment.getSlot().getType(),
                cancelLink
        );

        turboSmsProvider.sendSms(appointment.getClientPhone(), text);
    }
}
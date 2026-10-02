package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.entity.Appointment;

public interface NotificationService {

    void sendAppointmentConfirmation(Appointment appointment);
}

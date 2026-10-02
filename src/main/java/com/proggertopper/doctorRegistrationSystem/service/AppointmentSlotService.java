package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.dto.*;
import com.proggertopper.doctorRegistrationSystem.entity.*;
import com.proggertopper.doctorRegistrationSystem.exception.SlotAlreadyTakenException;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentRepository;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentSlotRepository;
import com.proggertopper.doctorRegistrationSystem.util.PhoneUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final AppointmentSlotRepository slotRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final PhoneVerificationService phoneVerificationService;


    @Transactional()
    public List<AppointmentSlotResponse> getSlots(LocalDate date, AppointmentType type) {
        return slotRepository.findBySlotDateAndTypeOrderByStartTime(date, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }



    @Transactional
    public String confirmSlot(Long slotId, ConfirmSlotRequest request) {
        PhoneUtils.validateUkrainianPhone(request.clientPhone());
        String normalizedPhone = PhoneUtils.normalize(request.clientPhone());

        boolean hasActiveAppointment = appointmentRepository
                .existsByClientPhoneNormalizedAndStatus(
                        normalizedPhone,
                        AppointmentStatus.CREATED
                );

        if (hasActiveAppointment) {
            throw new SlotAlreadyTakenException(
                    "You already have an active appointment. Please cancel it or visit the doctor first."
            );
        }

        phoneVerificationService.verifyCode(
                request.clientPhone(),
                request.verificationCode()
        );

        int updated = slotRepository.bookFreeSlot(
                slotId,
                SlotStatus.FREE,
                SlotStatus.BOOKED
        );

        if (updated == 0) {
            throw new SlotAlreadyTakenException("This slot is already taken");
        }

        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotAlreadyTakenException("Slot not found"));

        String cancelToken = UUID.randomUUID().toString();

        Appointment appointment = Appointment.builder()
                .slot(slot)
                .clientName(request.clientName())
                .clientPhone(request.clientPhone())
                .clientPhoneNormalized(normalizedPhone)
                .comment(request.comment())
                .status(AppointmentStatus.CREATED)
                .cancelToken(cancelToken)
                .createdAt(LocalDateTime.now())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.sendAppointmentConfirmation(savedAppointment);

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(slotId, SlotStatus.BOOKED)
        );

        return cancelToken;
    }

    @Transactional
    public void completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndStatus(appointmentId, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Active appointment not found"));

        appointment.setStatus(AppointmentStatus.COMPLETED);
    }

    @Transactional
    public void cancelAppointmentByDoctor(Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndStatus(appointmentId, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Active appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        int updated = slotRepository.cancelBooking(
                appointment.getSlot().getId(),
                SlotStatus.BOOKED,
                SlotStatus.FREE
        );

        if (updated == 0) {
            throw new SlotAlreadyTakenException("Slot is not booked or does not exist");
        }

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(appointment.getSlot().getId(), SlotStatus.FREE)
        );
    }

    @Transactional
    public void markAppointmentNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndStatus(appointmentId, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Active appointment not found"));

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(appointment.getSlot().getId(), SlotStatus.BOOKED)
        );
    }

    @Transactional
    public int createSlots(CreateSlotsRequest request) {
        validateCreateSlotsRequest(request);

        LocalTime current = request.startTime();
        int created = 0;

        while (current.plusMinutes(request.durationMinutes())
                .compareTo(request.endTime()) <= 0) {

            boolean exists = slotRepository.existsBySlotDateAndStartTimeAndType(
                    request.slotDate(),
                    current,
                    request.type()
            );

            if (!exists) {
                AppointmentSlot slot = AppointmentSlot.builder()
                        .slotDate(request.slotDate())
                        .startTime(current)
                        .endTime(current.plusMinutes(request.durationMinutes()))
                        .type(request.type())
                        .status(SlotStatus.FREE)
                        .createdAt(LocalDateTime.now())
                        .build();

                slotRepository.save(slot);
                created++;
            }

            current = current.plusMinutes(request.durationMinutes());
        }

        if (created > 0) {
            messagingTemplate.convertAndSend(
                    "/topic/slots",
                    new SlotUpdateEvent(null, SlotStatus.FREE)
            );
        }

        return created;
    }

    private void validateCreateSlotsRequest(CreateSlotsRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        long intervalMinutes = java.time.Duration.between(
                request.startTime(),
                request.endTime()
        ).toMinutes();

        if (request.durationMinutes() > intervalMinutes) {
            throw new IllegalArgumentException("Duration must fit inside selected time interval");
        }

        if (intervalMinutes % request.durationMinutes() != 0) {
            throw new IllegalArgumentException("Time interval must be divisible by appointment duration");
        }
    }

    @Transactional
    public void deleteFreeSlot(Long slotId) {
        int deleted = slotRepository.deleteFreeSlot(slotId, SlotStatus.FREE);

        if (deleted == 0) {
            throw new SlotAlreadyTakenException("Only FREE slots can be deleted");
        }

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(slotId, SlotStatus.FREE)
        );
    }

    @Transactional
    public void cancelBooking(Long slotId, String cancelToken) {
        Appointment appointment = appointmentRepository
                .findBySlotIdAndStatus(slotId, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Active appointment not found"));

        if (!appointment.getCancelToken().equals(cancelToken)) {
            throw new SlotAlreadyTakenException("Cancel token is invalid");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        int updated = slotRepository.cancelBooking(
                slotId,
                SlotStatus.BOOKED,
                SlotStatus.FREE
        );

        if (updated == 0) {
            throw new SlotAlreadyTakenException("Slot is not booked or does not exist");
        }

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(slotId, SlotStatus.FREE)
        );
    }

    @Transactional()
    public List<DoctorAppointmentResponse> getDoctorAppointments(LocalDate date) {
        return appointmentRepository.findBySlotSlotDateOrderBySlotStartTime(date)
                .stream()
                .map(appointment -> new DoctorAppointmentResponse(
                        appointment.getId(),
                        appointment.getSlot().getId(),
                        appointment.getSlot().getSlotDate(),
                        appointment.getSlot().getStartTime(),
                        appointment.getSlot().getEndTime(),
                        appointment.getSlot().getType(),
                        appointment.getClientName(),
                        appointment.getClientPhone(),
                        appointment.getComment(),
                        appointment.getStatus()
                ))
                .toList();
    }

    @Transactional()
    public CancelInfoResponse getCancelInfo(String token) {
        Appointment appointment = appointmentRepository
                .findByCancelTokenAndStatus(token, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Appointment not found or already cancelled"));

        return new CancelInfoResponse(
                appointment.getSlot().getSlotDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                appointment.getSlot().getType()
        );
    }

    @Transactional
    public void cancelBookingByToken(String token) {
        Appointment appointment = appointmentRepository
                .findByCancelTokenAndStatus(token, AppointmentStatus.CREATED)
                .orElseThrow(() -> new SlotAlreadyTakenException("Appointment not found or already cancelled"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        int updated = slotRepository.cancelBooking(
                appointment.getSlot().getId(),
                SlotStatus.BOOKED,
                SlotStatus.FREE
        );

        if (updated == 0) {
            throw new SlotAlreadyTakenException("Slot is not booked or does not exist");
        }

        messagingTemplate.convertAndSend(
                "/topic/slots",
                new SlotUpdateEvent(appointment.getSlot().getId(), SlotStatus.FREE)
        );
    }

    private AppointmentSlotResponse toResponse(AppointmentSlot slot) {
        return new AppointmentSlotResponse(
                slot.getId(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getType(),
                slot.getStatus()
        );
    }
}

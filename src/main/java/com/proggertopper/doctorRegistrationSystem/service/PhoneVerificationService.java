package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.entity.AppointmentStatus;
import com.proggertopper.doctorRegistrationSystem.entity.PhoneVerification;
import com.proggertopper.doctorRegistrationSystem.exception.SlotAlreadyTakenException;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentRepository;
import com.proggertopper.doctorRegistrationSystem.repository.PhoneVerificationRepository;
import com.proggertopper.doctorRegistrationSystem.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TurboSmsProvider turboSmsProvider;
    private final AppointmentRepository appointmentRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public void sendCode(String phone) {
        PhoneUtils.validateUkrainianPhone(phone);
        String normalizedPhone = PhoneUtils.normalize(phone);

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

        boolean sentRecently = phoneVerificationRepository
                .existsByPhoneAndCreatedAtAfter(
                        normalizedPhone,
                        LocalDateTime.now().minusMinutes(1)
                );

        if (sentRecently) {
            throw new SlotAlreadyTakenException(
                    "Sms code was already sent recently"
            );
        }

        long codesLastHour = phoneVerificationRepository
                .countByPhoneAndCreatedAtAfter(
                        normalizedPhone,
                        LocalDateTime.now().minusHours(1)
                );

        if (codesLastHour >= 5) {
            throw new SlotAlreadyTakenException(
                    "Too many sms codes requested"
            );
        }

        String code = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);

        PhoneVerification verification = PhoneVerification.builder()
                .phone(normalizedPhone)
                .code(code)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();

        phoneVerificationRepository.save(verification);

        turboSmsProvider.sendSms(
                phone,
                "Ваш код підтвердження: " + code
        );
    }

    @Transactional
    public void verifyCode(String phone, String code) {
        PhoneUtils.validateUkrainianPhone(phone);
        String normalizedPhone = PhoneUtils.normalize(phone);

        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneAndCodeAndUsedFalseOrderByCreatedAtDesc(
                        normalizedPhone,
                        code
                )
                .orElseThrow(() -> new SlotAlreadyTakenException("Invalid verification code"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new SlotAlreadyTakenException("Verification code expired");
        }

        verification.setUsed(true);
    }
}

package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.dto.CreateSlotsRequest;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentSlot;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentRepository;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotServiceTest {

    @Mock
    private AppointmentSlotRepository slotRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @InjectMocks
    private AppointmentSlotService service;

    @Test
    void createsSlotsForWholeInterval() {
        LocalDate date = LocalDate.now().plusDays(1);
        CreateSlotsRequest request = new CreateSlotsRequest(
                date,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                30,
                AppointmentType.ONLINE
        );

        when(slotRepository.existsBySlotDateAndStartTimeAndType(eq(date), any(), eq(AppointmentType.ONLINE)))
                .thenReturn(false);

        int created = service.createSlots(request);

        assertThat(created).isEqualTo(2);

        ArgumentCaptor<AppointmentSlot> captor = ArgumentCaptor.forClass(AppointmentSlot.class);
        verify(slotRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(AppointmentSlot::getStartTime)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(10, 30));
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        CreateSlotsRequest request = new CreateSlotsRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(12, 0),
                LocalTime.of(11, 0),
                30,
                AppointmentType.ONLINE
        );

        assertThatThrownBy(() -> service.createSlots(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must be after start time");

        verify(slotRepository, never()).save(any());
    }

    @Test
    void rejectsIntervalThatIsNotDivisibleByDuration() {
        CreateSlotsRequest request = new CreateSlotsRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 50),
                30,
                AppointmentType.ONLINE
        );

        assertThatThrownBy(() -> service.createSlots(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time interval must be divisible by appointment duration");

        verify(slotRepository, never()).save(any());
    }
}

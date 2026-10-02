package com.proggertopper.doctorRegistrationSystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proggertopper.doctorRegistrationSystem.dto.AdminLoginRequest;
import com.proggertopper.doctorRegistrationSystem.dto.ConfirmSlotRequest;
import com.proggertopper.doctorRegistrationSystem.dto.CreateSlotsRequest;
import com.proggertopper.doctorRegistrationSystem.dto.SendOtpRequest;
import com.proggertopper.doctorRegistrationSystem.entity.Appointment;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentStatus;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import com.proggertopper.doctorRegistrationSystem.entity.PhoneVerification;
import com.proggertopper.doctorRegistrationSystem.entity.SlotStatus;
import com.proggertopper.doctorRegistrationSystem.repository.AdminSessionRepository;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentRepository;
import com.proggertopper.doctorRegistrationSystem.repository.AppointmentSlotRepository;
import com.proggertopper.doctorRegistrationSystem.repository.PhoneVerificationRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowIntegrationTest {

    private static final String PATIENT_PHONE = "+380934234243";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PhoneVerificationRepository phoneVerificationRepository;

    @Autowired
    private AdminSessionRepository adminSessionRepository;

    @BeforeEach
    void cleanDatabase() {
        appointmentRepository.deleteAll();
        slotRepository.deleteAll();
        phoneVerificationRepository.deleteAll();
        adminSessionRepository.deleteAll();
    }

    @Test
    void booksAndCancelsAppointmentByToken() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        Cookie adminCookie = loginAsDoctor();

        mockMvc.perform(post("/api/slots")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CreateSlotsRequest(
                                date,
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0),
                                30,
                                AppointmentType.ONLINE
                        ))))
                .andExpect(status().isOk());

        Long slotId = slotRepository.findBySlotDateAndTypeOrderByStartTime(date, AppointmentType.ONLINE)
                .getFirst()
                .getId();

        mockMvc.perform(get("/api/slots")
                        .param("date", date.toString())
                        .param("type", AppointmentType.ONLINE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(SlotStatus.FREE.name()))
                .andExpect(jsonPath("$[1].status").value(SlotStatus.FREE.name()));

        mockMvc.perform(post("/api/phone/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SendOtpRequest(PATIENT_PHONE))))
                .andExpect(status().isOk());

        PhoneVerification verification = phoneVerificationRepository.findAll().getFirst();

        mockMvc.perform(post("/api/slots/{slotId}/confirm", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmSlotRequest(
                                "Ivan Petrenko",
                                PATIENT_PHONE,
                                verification.getCode(),
                                "First consultation"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(slotId));

        Appointment appointment = appointmentRepository.findAll().getFirst();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CREATED);
        assertThat(slotRepository.findById(slotId).orElseThrow().getStatus()).isEqualTo(SlotStatus.BOOKED);

        mockMvc.perform(get("/api/slots/cancel-info")
                        .param("token", appointment.getCancelToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(AppointmentType.ONLINE.name()));

        mockMvc.perform(post("/api/slots/cancel-by-token")
                        .param("token", appointment.getCancelToken()))
                .andExpect(status().isOk());

        Appointment cancelledAppointment = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(cancelledAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(slotRepository.findById(slotId).orElseThrow().getStatus()).isEqualTo(SlotStatus.FREE);
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("GastroCare Doctor Appointment API"))
                .andExpect(jsonPath("$.paths['/api/slots']").exists())
                .andExpect(jsonPath("$.paths['/api/phone/send-code']").exists());
    }

    private Cookie loginAsDoctor() throws Exception {
        return mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AdminLoginRequest("12345"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("DOCTOR_SESSION");
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}

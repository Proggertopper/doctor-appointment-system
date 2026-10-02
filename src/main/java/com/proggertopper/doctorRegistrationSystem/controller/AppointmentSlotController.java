package com.proggertopper.doctorRegistrationSystem.controller;

import com.proggertopper.doctorRegistrationSystem.dto.*;
import com.proggertopper.doctorRegistrationSystem.entity.AppointmentType;
import com.proggertopper.doctorRegistrationSystem.exception.SlotAlreadyTakenException;
import com.proggertopper.doctorRegistrationSystem.service.AppointmentSlotService;
import com.proggertopper.doctorRegistrationSystem.service.PhoneVerificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
public class AppointmentSlotController {

    private final AppointmentSlotService slotService;

    @GetMapping
    public List<AppointmentSlotResponse> getSlots(
            @RequestParam LocalDate date,
            @RequestParam AppointmentType type
    ) {
        return slotService.getSlots(date, type);
    }



    @PostMapping("/{slotId}/confirm")
    public ConfirmSlotResponse confirmSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody ConfirmSlotRequest request,
            HttpServletResponse response
    ) {
        String cancelToken = slotService.confirmSlot(slotId, request);

        Cookie cookie = new Cookie("cancel_token_" + slotId, cancelToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);

        response.addCookie(cookie);

        return new ConfirmSlotResponse(slotId);
    }

    @PostMapping("/doctor/appointments/{appointmentId}/complete")
    public void completeAppointment(@PathVariable Long appointmentId) {
        slotService.completeAppointment(appointmentId);
    }

    @PostMapping("/doctor/appointments/{appointmentId}/cancel")
    public void cancelAppointmentByDoctor(@PathVariable Long appointmentId) {
        slotService.cancelAppointmentByDoctor(appointmentId);
    }

    @PostMapping("/doctor/appointments/{appointmentId}/no-show")
    public void markAppointmentNoShow(@PathVariable Long appointmentId) {
        slotService.markAppointmentNoShow(appointmentId);
    }

    @PostMapping("/{slotId}/cancel")
    public void cancelBooking(
            @PathVariable Long slotId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String cookieName = "cancel_token_" + slotId;

        String cancelToken = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .orElseThrow(() -> new SlotAlreadyTakenException("Cancel token not found"))
                .getValue();

        slotService.cancelBooking(slotId, cancelToken);

        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    @GetMapping("/doctor/appointments")
    public List<DoctorAppointmentResponse> getDoctorAppointments(
            @RequestParam LocalDate date
    ) {
        return slotService.getDoctorAppointments(date);
    }

    @PostMapping
    public String createSlots(@Valid @RequestBody CreateSlotsRequest request) {
        int created = slotService.createSlots(request);
        return "Created slots: " + created;
    }

    @DeleteMapping("/{slotId}")
    public void deleteFreeSlot(@PathVariable Long slotId) {
        slotService.deleteFreeSlot(slotId);
    }

    @GetMapping("/cancel-info")
    public CancelInfoResponse getCancelInfo(@RequestParam String token) {
        return slotService.getCancelInfo(token);
    }

    @PostMapping("/cancel-by-token")
    public void cancelByToken(@RequestParam String token) {
        slotService.cancelBookingByToken(token);
    }
}

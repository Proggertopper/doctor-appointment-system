package com.proggertopper.doctorRegistrationSystem.controller;

import com.proggertopper.doctorRegistrationSystem.dto.SendOtpRequest;
import com.proggertopper.doctorRegistrationSystem.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/phone")
public class PhoneVerificationController {

    private final PhoneVerificationService service;

    @PostMapping("/send-code")
    public void sendCode(@Valid @RequestBody SendOtpRequest request) {
        service.sendCode(request.phone());
    }
}

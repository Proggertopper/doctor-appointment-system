package com.proggertopper.doctorRegistrationSystem.controller;


import com.proggertopper.doctorRegistrationSystem.dto.AdminLoginRequest;
import com.proggertopper.doctorRegistrationSystem.entity.AdminSession;
import com.proggertopper.doctorRegistrationSystem.exception.SlotAlreadyTakenException;
import com.proggertopper.doctorRegistrationSystem.repository.AdminSessionRepository;
import com.proggertopper.doctorRegistrationSystem.service.AdminLoginAttemptService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminSessionRepository adminSessionRepository;
    private final AdminLoginAttemptService adminLoginAttemptService;
    private final PasswordEncoder passwordEncoder;

    @Value("${doctor.admin.password-hash}")
    private String adminPasswordHash;

    @Value("${doctor.admin.cookie-name}")
    private String cookieName;

    @Value("${doctor.admin.session-hours}")
    private int sessionHours;

    @Value("${doctor.admin.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public void login(
            @Valid @RequestBody AdminLoginRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String ip = getClientIp(request);

        if (adminLoginAttemptService.isBlocked(ip)) {
            throw new SlotAlreadyTakenException(
                    "Too many login attempts. Try again later."
            );
        }

        if (!passwordEncoder.matches(requestBody.password(), adminPasswordHash)) {
            adminLoginAttemptService.loginFailed(ip);
            throw new SlotAlreadyTakenException("Invalid admin password");
        }

        adminLoginAttemptService.loginSucceeded(ip);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(sessionHours);

        AdminSession session = AdminSession.builder()
                .token(token)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        adminSessionRepository.save(session);

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(sessionHours))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (token != null) {
            adminSessionRepository.deleteByToken(token);
        }

        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

}

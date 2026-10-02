package com.proggertopper.doctorRegistrationSystem.config;

import com.proggertopper.doctorRegistrationSystem.repository.AdminSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminSessionAuthenticationFilter extends OncePerRequestFilter {

    private final AdminSessionRepository adminSessionRepository;

    @Value("${doctor.admin.cookie-name}")
    private String cookieName;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (token != null) {
            adminSessionRepository.findByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                    .ifPresent(session -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        "doctor",
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}

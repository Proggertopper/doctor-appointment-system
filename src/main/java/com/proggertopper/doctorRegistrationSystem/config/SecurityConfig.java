package com.proggertopper.doctorRegistrationSystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminSessionAuthenticationFilter adminSessionAuthenticationFilter;
    private final SecurityRedirectEntryPoint securityRedirectEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/doctor.html", "/doctor-slots.html").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/slots/doctor/appointments").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/slots").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/slots/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/slots/doctor/appointments/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(securityRedirectEntryPoint))
                .addFilterBefore(adminSessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

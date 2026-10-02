package com.proggertopper.doctorRegistrationSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gastroCareOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GastroCare Doctor Appointment API")
                        .version("1.0.0")
                        .description("API for appointment slots, patient booking, phone verification, cancellations, and doctor administration.")
                        .license(new License().name("Portfolio project")))
                .components(new Components()
                        .addSecuritySchemes("doctorSession", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("DOCTOR_SESSION")
                                .description("Doctor admin session cookie returned by /api/admin/auth/login.")));
    }
}

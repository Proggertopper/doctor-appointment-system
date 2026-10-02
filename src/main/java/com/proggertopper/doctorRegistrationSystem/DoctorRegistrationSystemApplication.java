package com.proggertopper.doctorRegistrationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DoctorRegistrationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorRegistrationSystemApplication.class, args);
	}

}

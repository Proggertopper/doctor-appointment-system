package com.proggertopper.doctorRegistrationSystem.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminLoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES = 15;

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        LoginAttempt attempt = attempts.get(ip);

        if (attempt == null) {
            return false;
        }

        if (attempt.blockedUntil == null) {
            return false;
        }

        if (attempt.blockedUntil.isBefore(LocalDateTime.now())) {
            attempts.remove(ip);
            return false;
        }

        return true;
    }

    public void loginFailed(String ip) {
        LoginAttempt attempt = attempts.getOrDefault(ip, new LoginAttempt());

        attempt.count++;

        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_MINUTES);
        }

        attempts.put(ip, attempt);
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    private static class LoginAttempt {
        private int count = 0;
        private LocalDateTime blockedUntil;
    }
}

package com.proggertopper.doctorRegistrationSystem.service;

import com.proggertopper.doctorRegistrationSystem.dto.TurboSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurboSmsProvider {

    private final RestTemplate restTemplate;

    @Value("${turbosms.api-token}")
    private String apiToken;

    @Value("${turbosms.sms-sender}")
    private String smsSender;

    @Value("${turbosms.enabled:false}")
    private boolean enabled;

    public void sendSms(String phone, String text) {
        if (!enabled) {
            log.info("""
                    
                    TURBOSMS SMS MOCK
                    To: {}
                    Sender: {}
                    Text:
                    {}
                    """, phone, smsSender, text);
            return;
        }

        String url = "https://api.turbosms.ua/message/send.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> body = Map.of(
                "recipients", List.of(phone),
                "sms", Map.of(
                        "sender", smsSender,
                        "text", text
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TurboSmsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                TurboSmsResponse.class
        );

        TurboSmsResponse result = response.getBody();

        if (result == null || result.response_code() == null || result.response_code() != 0) {
            throw new IllegalStateException("TurboSMS error: " + result);
        }

        log.info("TurboSMS SMS sent to {}", phone);
    }
}
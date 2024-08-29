package com.idirtrack.backend.traccar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TracCarSessionService {

    @Value("${traccar.api.url}")
    private String url;

    private final RestTemplate restTemplate;

        // Create Loger
    private static final Logger logger = LoggerFactory.getLogger(TracCarSessionService.class);

    public String createSession(String email, String password) {
        String sessionEndpoint = url + "/session";

        // Create the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        // Create the request body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", email);
        formData.add("password", password);

        // Create the request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.exchange(sessionEndpoint, HttpMethod.POST, requestEntity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Session created successfully");

            // Extract the Set-Cookie header
            String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            if (setCookieHeader != null) {
                // Parse the cookies and extract the JSESSIONID
                for (String cookie : setCookieHeader.split(";")) {
                    if (cookie.trim().startsWith("JSESSIONID=")) {
                        String sessionId = cookie.split("=")[1];
                        return sessionId; // Return the session ID
                    }
                }
            }
        } else {
            System.out.println("Failed to create session");
        }

        return null;
    }
}

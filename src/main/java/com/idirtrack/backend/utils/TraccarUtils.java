package com.idirtrack.backend.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.jwt.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraccarUtils {

    private final JwtUtils jwtUtils;

    public HttpHeaders createHeadersFromToken(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Extract the session ID from the JWT token
        String sessionId = jwtUtils.extractSession(jwtToken);
        // Check if the session ID is not null
        if (sessionId != null) {
            // Add the session ID as a cookie in the headers
            headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + sessionId);
        } else {
            throw new IllegalArgumentException("Session ID not found in the JWT token.");
        }

        return headers;
    }
}

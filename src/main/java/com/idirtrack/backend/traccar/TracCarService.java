package com.idirtrack.backend.traccar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.traccar.request.TracCarDeviceRequest;
import com.idirtrack.backend.utils.TraccarUtils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TracCarService {

    private static final Logger logger = LoggerFactory.getLogger(TracCarService.class);

    private final RestTemplate restTemplate;
    private final TraccarUtils traccarUtils;
    private final JwtUtils jwtUtils;

    @Value("${traccar.api.url}")
    private String traccarUrl;

    // Create a device in Traccar
    public Long createDevice(TracCarDeviceRequest request, String authHeader) {
        String url = traccarUrl + "/devices";
        String token = jwtUtils.extractToken(authHeader);
        HttpHeaders headers = traccarUtils.createHeadersFromToken(token);
        HttpEntity<TracCarDeviceRequest> entity = new HttpEntity<>(request, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("id")) {
                Long traccarId = Long.parseLong(response.get("id").toString());
                logger.debug("Device created successfully: {}", response);
                return traccarId;
            } else {
                logger.debug("Error: Received null or invalid response from Traccar");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error creating device: {}", e.getMessage());
            return null;
        }
    }

    // Update a device in Traccar
    public Long updateDevice(TracCarDeviceRequest request, String authHeader, Long id) {
        String url = traccarUrl + "/devices/" + id;
        String token = jwtUtils.extractToken(authHeader);
        HttpHeaders headers = traccarUtils.createHeadersFromToken(token);
        HttpEntity<TracCarDeviceRequest> entity = new HttpEntity<>(request, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("id")) {
                Long traccarId = Long.parseLong(response.get("id").toString());
                logger.debug("Device updated successfully: {}", response);
                return traccarId;
            } else {
                logger.debug("Error: Received null or invalid response from Traccar");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error updating device: {}", e.getMessage());
            return null;
        }
    }

    // Delete a device in Traccar
    public boolean deleteDevice(Long traccarId, String authHeader) {
        String url = traccarUrl + "/devices/" + traccarId;
        String token = jwtUtils.extractToken(authHeader);
        HttpHeaders headers = traccarUtils.createHeadersFromToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Device deleted successfully with ID: {}", traccarId);
                return true;
            } else {
                logger.debug("Failed to delete device with ID: {}. Status code: {}", traccarId, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting device with ID {}: {}", traccarId, e.getMessage());
            return false;
        }
    }
}

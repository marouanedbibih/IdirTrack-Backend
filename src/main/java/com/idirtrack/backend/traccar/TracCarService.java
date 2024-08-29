package com.idirtrack.backend.traccar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.idirtrack.backend.traccar.dto.TCRDeviceRequest;
import com.idirtrack.backend.traccar.request.TracCarDeviceRequest;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class TracCarService {

    private final RestTemplate restTemplate;
    @Value("${traccar.api.url}")
    private String traccarUrl;

    private static final Logger logger = LoggerFactory.getLogger(TracCarService.class);

    public Long createDevice(String clientName, String imei, String clientCompany, String vehicleMatricule) {
        String url = traccarUrl + "/devices";

        String name = clientName + " - " + clientCompany + " - " + vehicleMatricule;

        // Create the request body
        TracCarDeviceRequest request = TracCarDeviceRequest.builder()
                .name(name)
                .uniqueId(imei)
                .build();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // add basic auth with username and password idirtech idirtech
        headers.setBasicAuth("idirtech ", "idirtech1");

        // Create the HttpEntity
        HttpEntity<TracCarDeviceRequest> entity = new HttpEntity<>(request, headers);

        // Send the POST request
        // Send the POST request and handle the response
        try {
            // Send the request and get the response as a Map
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("id")) {
                // Extract and return the ID from the response
                logger.debug("Device created successfully: {}", response);
                Long traccarId = Long.parseLong(response.get("id").toString());
                logger.debug("Device TracCar ID: {}", traccarId);
                return traccarId;
            } else {
                logger.debug("Error: Received null or invalid response from Traccar");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error creating device: " + e.getMessage());
            return null;
        }
    }

    public Long updateDevice(TCRDeviceRequest tcrDevice) {
        String url = traccarUrl + "/devices/" + tcrDevice.getTraccarId();

        String name = tcrDevice.getClientName() + " - " + tcrDevice.getClientCompany() + " - "
                + tcrDevice.getVehicleMatricule();

        // Create the request body
        TracCarDeviceRequest request = TracCarDeviceRequest.builder()
                .name(name)
                .uniqueId(tcrDevice.getImei())
                .build();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // add basic auth with username and password idirtech idirtech
        headers.setBasicAuth("idirtech ", "idirtech1");

        // Create the HttpEntity
        HttpEntity<TracCarDeviceRequest> entity = new HttpEntity<>(request, headers);

        // Send the POST request
        // Send the POST request and handle the response
        try {
            // Send the request and get the response as a Map
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.containsKey("id")) {
                // Extract and return the ID from the response
                logger.debug("Device created successfully: {}", response);
                Long traccarId = Long.parseLong(response.get("id").toString());
                logger.debug("Device TracCar ID: {}", traccarId);
                return traccarId;
            } else {
                logger.debug("Error: Received null or invalid response from Traccar");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error creating device: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteDevice(Long traccarId) {
        String url = traccarUrl + "/devices/" + traccarId;

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("idirtech", "idirtech1");

        // Create the HttpEntity with the headers only (no body is needed for DELETE)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Send the DELETE request
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);

            // Check if the response status code indicates success (204 No Content is
            // expected for a successful delete)
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Device deleted successfully with ID: {}", traccarId);
                return true;
            } else {
                logger.debug("Failed to delete device with ID: {}. Status code: {}", traccarId,
                        response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting device with ID {}: {}", traccarId, e.getMessage());
            return false;
        }
    }

}

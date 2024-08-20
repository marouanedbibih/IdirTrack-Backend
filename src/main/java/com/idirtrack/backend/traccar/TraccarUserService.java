package com.idirtrack.backend.traccar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraccarUserService {

    private final RestTemplate restTemplate;
    private final TracCarUser tracCarUser;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraccarUserService.class);

    public Map<String, Object> createAdmin(UserDTO user) throws BasicException {
        String url = "http://152.228.219.146:8082/api/users";

        // Create the request body from TracCarUser
        TracCarUser admin = TracCarUser.buildAdminUser(user);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // add basic auth with username and password idirtech idirtech1
        headers.setBasicAuth("idirtech", "idirtech1");

        // Create the HttpEntity
        HttpEntity<TracCarUser> entity = new HttpEntity<>(admin, headers);

        // Send the POST request
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            logger.info("Admin user created successfully: {}", response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

    public Map<String, Object> createUser(UserDTO user, UserRole role) throws BasicException {
        String url = "http://152.228.219.146:8082/api/users";

        // Create the request body from TracCarUser
        // TracCarUser admin = TracCarUser.buildAdminUser(user);
        TracCarUser tracCarUser = new TracCarUser();
        if (role == UserRole.ADMIN) {
            tracCarUser = TracCarUser.buildAdminUser(user);
        } else if (role == UserRole.MANAGER) {
            tracCarUser = TracCarUser.buildManagerUser(user);
        } else if (role == UserRole.CLIENT) {
            // TracCarUser user = TracCarUser.buildUser(user);
        }

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // add basic auth with username and password idirtech idirtech1
        headers.setBasicAuth("idirtech", "idirtech1");

        // Create the HttpEntity
        HttpEntity<TracCarUser> entity = new HttpEntity<>(tracCarUser, headers);

        // Send the POST request
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            logger.info("Admin user created successfully: {}", response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

    public Map<String, Object> updateUser(UserDTO user) throws BasicException {
        String url = "http://152.228.219.146:8082/api/users/" + user.getTraccarId();

        // Create the request body based on the user's role
        TracCarUser tracCarUser = new TracCarUser();
        if (user.getRole() == UserRole.ADMIN) {
            tracCarUser = TracCarUser.buildAdminUser(user);
        } else if (user.getRole() == UserRole.MANAGER) {
            tracCarUser = TracCarUser.buildManagerUser(user);
        } else if (user.getRole() == UserRole.CLIENT) {
            // tracCarUser = TracCarUser.buildClientUser(user);
        }

        tracCarUser.setId(user.getTraccarId());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("idirtech", "idirtech1"); // Basic auth with username and password

        // Create the HttpEntity
        HttpEntity<TracCarUser> entity = new HttpEntity<>(tracCarUser, headers);

        // Send the PUT request
        try {
            restTemplate.put(url, entity);
            logger.info("User updated successfully: {}", user.getTraccarId());
            return Map.of("status", "success", "message", "User updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update user with ID {}: {}", user.getTraccarId(), e.getMessage());
            throw new BasicException(BasicResponse.builder()
                    .message("Failed to update user: " + e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    public boolean deleteUser(Long traccarId) throws BasicException {
        String url = "http://152.228.219.146:8082/api/users/" + traccarId;

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("idirtech", "idirtech1"); // Basic auth with username and password

        // Create the HttpEntity
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Send the DELETE request
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            logger.info("User deleted successfully: {}", traccarId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete user with ID {}: {}", traccarId, e.getMessage());
            throw new BasicException(BasicResponse.builder()
                    .message("Failed to delete user: " + e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

}

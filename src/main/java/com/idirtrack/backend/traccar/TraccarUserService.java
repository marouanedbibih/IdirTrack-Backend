package com.idirtrack.backend.traccar;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.TraccarUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraccarUserService {

    private final RestTemplate restTemplate;
    private final TraccarUtils traccarUtils;
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraccarUserService.class);

    @Value("${traccar.api.url}")
    private String traccarUrl;

    // Create a user in Traccar
    public Long createUser(UserDTO user, String bearerToken) throws MyException {
        String url = traccarUrl + "/users";

        // Create the request body based on the user's role
        TracCarUser requestBody = new TracCarUser();
        if (user.getRole() == UserRole.ADMIN) {
            requestBody = TracCarUser.buildAdminUser(user);
        } else if (user.getRole() == UserRole.MANAGER) {
            requestBody = TracCarUser.buildManagerUser(user);
        } else if (user.getRole() == UserRole.CLIENT) {
            requestBody = TracCarUser.buildClient(user);
        }

        // Config the authorization header for traccar request
        HttpHeaders requestHeader = traccarUtils.createHeadersFromBearerToken(bearerToken);
        HttpEntity<TracCarUser> requestEntity = new HttpEntity<>(requestBody, requestHeader);

        // Send the POST request
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);
            // Cast the response to a map and get the id
            Long traccarUserId = Long.parseLong(response.get("id").toString());
            return traccarUserId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to create user in Traccar :" + e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

    // Update user in Traccar
    public void updateUser(UserDTO user, String bearerToken) throws MyException {
        String url = traccarUrl + "/users/" + user.getTraccarId();

        // Create the request body based on the user's role
        TracCarUser requestBody = new TracCarUser();
        if (user.getRole() == UserRole.ADMIN) {
            requestBody = TracCarUser.buildAdminUser(user);
        } else if (user.getRole() == UserRole.MANAGER) {
            requestBody = TracCarUser.buildManagerForUpdate(user);
        } else if (user.getRole() == UserRole.CLIENT) {
            requestBody = TracCarUser.buildClientForUpdate(user);
        }

        // Create the request header with the bearer token
        HttpHeaders requestHeader = traccarUtils.createHeadersFromBearerToken(bearerToken);
        HttpEntity<TracCarUser> requestEntity = new HttpEntity<>(requestBody, requestHeader);

        // Send the PUT request
        try {
            restTemplate.put(url, requestEntity);
        } catch (Exception e) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to update user in Traccar: " + e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    // Delete client from Traccar
    public void deleteUser(Long traccarUserId, String bearerToken) throws MyException {
        String url = traccarUrl + "/users/" + traccarUserId;
        // Config the authorization header for traccar request
        HttpHeaders headers = traccarUtils.createHeadersFromBearerToken(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MyException(ErrorResponse.builder()
                        .message("Failed to delete user in Traccar")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }
        } catch (Exception e) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to delete user in Traccar: " + e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }


}

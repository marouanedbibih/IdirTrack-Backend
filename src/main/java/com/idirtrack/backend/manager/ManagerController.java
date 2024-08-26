package com.idirtrack.backend.manager;

import java.net.http.HttpHeaders;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.manager.dtos.ManagerRequest;
import com.idirtrack.backend.manager.dtos.UpdateManagerRequest;
import com.idirtrack.backend.utils.BasicError;
import com.idirtrack.backend.utils.ValidationUtils;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.jwt.JwtUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    //call jwtUtils
    private final JwtUtils jwtUtils;


    /**
     * Endpoint to delete a manager by ID
     * 
     * @param id
     * @return BasicResponse
     * @throws BasicException
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}/")
    public ResponseEntity<BasicResponse> deleteManager(@PathVariable Long id) {
        try {
            BasicResponse response = managerService.deleteManager(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch BasicException
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any other exception
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Endpoint to get all managers with pagination
     * 
     * @param page
     * @param size
     * @return BasicResponse
     * @throws BasicException
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllManagers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestHeader("Authorization") String token) {
        try {

            BasicResponse response = managerService.getAllManagers(page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
            
        }
        // Catch BasicException
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any other exception
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Endpoint to get manager by ID
     * 
     * @param id
     * @return BasicResponse
     * @throws BasicException
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/")
    public ResponseEntity<BasicResponse> getManagerById(@PathVariable Long id) {
        try {
            BasicResponse response = managerService.getManagerById(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch BasicException
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any other exception
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Endpoint to update Manager
     * 
     * @param managerRequest
     * @param id
     * @return BasicResponse
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/")
    public ResponseEntity<BasicResponse> updateManager(
            @RequestBody @Valid UpdateManagerRequest managerRequest,
            @PathVariable Long id,
            BindingResult bindingResult) {
        try {
            BasicResponse response = managerService.updateManager(managerRequest, id);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch BasicException
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any other exception
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }

    }

    /**
     * Endpoint API to create a new manager
     * 
     * @param managerRequest
     * @param bindingResult
     * @param token
     * @return ResponseEntity<BasicResponse>
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<BasicResponse> createManager(
            @RequestBody @Valid ManagerRequest managerRequest,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {

        // Validate the request
        if (bindingResult.hasErrors()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .messageType(MessageType.ERROR)
                            .build());
        }

        try {
            // Remove "Bearer " from the token and trim any spaces
            String jwtToken = token.replace("Bearer ", "").trim();

            // Call the service to create the manager
            BasicResponse response = managerService.createManager(managerRequest, jwtToken);

            // Extract the session from the JWT token
            String session = jwtUtils.extractSession(jwtToken);
            // Create a ResponseCookie with the session ID
            ResponseCookie sessionCookie = ResponseCookie.from("JSESSIONID", session)
                    .httpOnly(true)
                    .path("/")
                    .build();

               //print the session cookie
               System.out.println("hanaaaa " + sessionCookie);
               // Return the response with the session cookie in the headers
               return ResponseEntity.status(response.getStatus())
               .header("Set-Cookie", sessionCookie.toString())
               .body(response);

        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse
                            .builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }
}

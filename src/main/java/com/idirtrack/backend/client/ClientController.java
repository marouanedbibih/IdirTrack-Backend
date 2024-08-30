package com.idirtrack.backend.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.utils.ValidationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.idirtrack.backend.client.dtos.ClientRequest;


@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {
  
      private final ClientService clientService;
    //   private final ClientRequest clientRequest;
    //   //call jwtUtils
    private final JwtUtils jwtUtils;

    /**
     * Endpoint to create a client
      * @param clientRequest
     * @param bindingResult
     * @param token
     * @return ResponseEntity<BasicResponse>
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/")
    public ResponseEntity<BasicResponse> createClient(
            @RequestBody @Valid ClientRequest clientRequest,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {

//         // Validate the request
        if (bindingResult.hasErrors()) {
            return ValidationUtils.handleValidationErrors(bindingResult);
        }

        try {
            // Remove "Bearer " from the token and trim any spaces
            String jwtToken = token.replace("Bearer ", "").trim();

            // Call the service to create the manager
            BasicResponse response = clientService.createClient(clientRequest, jwtToken);

            // Extract the session from the JWT token
            String session = jwtUtils.extractSession(jwtToken);
            // Create a ResponseCookie with the session ID
            ResponseCookie sessionCookie = ResponseCookie.from("JSESSIONID", session)
                    .httpOnly(true)
                    .path("/")
                    .build();
            //print the session cookie
            System.out.println("hanaaaa " + sessionCookie);
//             // Return the response with the session cookie in the headers
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

    //get all clients with pagination
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllClients(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestHeader("Authorization") String token){
            try {
                BasicResponse response = clientService.getAllClients(page, size);
                 return ResponseEntity.status(response.getStatus()).body(response);

            }
            catch (BasicException e) {
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
    
    //search clients
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<BasicResponse> searchClients(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader("Authorization") String token){
            try {
                BasicResponse response = clientService.searchClients(keyword, page, size);
                 return ResponseEntity.status(response.getStatus()).body(response);

            }
            catch (BasicException e) {
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


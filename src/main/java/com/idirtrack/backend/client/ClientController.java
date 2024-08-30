package com.idirtrack.backend.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.ValidationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.idirtrack.backend.client.dtos.ClientCategoryDto;
import com.idirtrack.backend.client.dtos.ClientRequest;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientCategoryService  categoryService;
    private final ClientService clientService;
    // private final ClientRequest clientRequest;
    // //call jwtUtils
    private final JwtUtils jwtUtils;

    // Get list of clients to use in select dropdown
    @GetMapping("/dropdown")
    public ResponseEntity<MyResponse> getClientsDropdown() {
        MyResponse response = clientService.getClientsDropdown();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Endpoint to create a client
     * 
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

        // // Validate the request
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
            // print the session cookie
            System.out.println("hanaaaa " + sessionCookie);
            // // Return the response with the session cookie in the headers
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

    // get all clients with pagination
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllClients(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestHeader("Authorization") String token) {
        try {
            BasicResponse response = clientService.getAllClients(page, size);
            return ResponseEntity.status(response.getStatus()).body(response);

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

    // search clients
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


        //delete client
         @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponse> deleteClient(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            clientService.deleteClient(id, token);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(BasicResponse.builder()
                            .message("Client deleted successfully")
                            .status(HttpStatus.OK)
                            .build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BasicResponse.builder()
                            .message("Client not found with id: " + id)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/categories/{id}")
    public ResponseEntity<ClientCategory> getClientCategoryById(@PathVariable Long id) {
        try {
            ClientCategory clientCategory = categoryService.getClientCategoryById(id);
            return ResponseEntity.ok(clientCategory);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/categories/")
    public ResponseEntity<BasicResponse> createClientCategory(@RequestBody @Valid ClientCategory clientCategory) {
        try {
            categoryService.createClientCategory(clientCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    BasicResponse.builder().message("Category created successfully").status(HttpStatus.CREATED).build());
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.CONFLICT).build());
        }
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<BasicResponse> updateClientCategory(@PathVariable Long id, @RequestBody @Valid ClientCategory clientCategoryDetails) {
        try {
            categoryService.updateClientCategory(id, clientCategoryDetails);
            return ResponseEntity.ok(
                    BasicResponse.builder().message("Category updated successfully").status(HttpStatus.OK).build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.NOT_FOUND).build());
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.CONFLICT).build());
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<BasicResponse> deleteClientCategory(@PathVariable Long id) {
        try {
            categoryService.deleteClientCategory(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(BasicResponse.builder()
                            .message("Client category deleted successfully")
                            .status(HttpStatus.OK)
                            .build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BasicResponse.builder()
                            .message("Client category not found with id: " + id)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }


    // Get total number of clients
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/total")
    public ResponseEntity<BasicResponse> getTotalClients() {
        long totalClients = clientService.getTotalClients();
        return ResponseEntity.status(HttpStatus.OK)
                .body(BasicResponse.builder()
                        .message("Total clients retrieved successfully")
                        .status(HttpStatus.OK)
                        .content(totalClients)
                        .build());
    }

    // Get all categories with pagination and total count of clients
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/categories/")
    public ResponseEntity<?> getCategoriesWithClientCount(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
    MyResponse response = categoryService.getCategoriesWithClientCount(page, size);
    return ResponseEntity.status(response.getStatus()).body(response);
}
}


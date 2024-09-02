package com.idirtrack.backend.client;

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
import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.RequestValidation;
import com.idirtrack.backend.utils.ValidationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.idirtrack.backend.client.dtos.ClientInfoDTO;
import com.idirtrack.backend.client.dtos.ClientRequest;
import com.idirtrack.backend.client.dtos.ClientUpdateRequest;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    

    // Get list of clients with pagination
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/clients")
    public ResponseEntity<MyResponse> getAllClients(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        MyResponse response = clientService.getListOfClients(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to search clients
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/clients/search")
    public ResponseEntity<MyResponse> searchClients(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        MyResponse response = clientService.searchClients(keyword, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Filter clients by category and is active
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/clients/filter")
    public ResponseEntity<MyResponse> filterClientsByCategoryAndStatus(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) boolean isDisabled,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        MyResponse response = clientService.filterClients(categoryId, isDisabled, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Get list of clients to use in select dropdown
    @GetMapping("/api/v1/clients/dropdown")
    public ResponseEntity<MyResponse> getClientsDropdown() {
        MyResponse response = clientService.getClientsDropdown();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Search clients to use in select dropdown
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/clients/dropdown/search")
    public ResponseEntity<MyResponse> searchClientsDropdown(
            @RequestParam(value = "keyword", required = false) String keyword) {
        MyResponse response = clientService.searchClientsDropdown(keyword);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    // Endpoint to create a new client
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/api/v1/client")
    public ResponseEntity<MyResponse> createClient(
            @RequestBody @Valid ClientRequest clientRequest,
            @RequestHeader("Authorization") String bearerToken) {

        MyResponse response = clientService.createClient(clientRequest, bearerToken);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Update client info
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PutMapping("/api/v1/client/{clientId}")
    public ResponseEntity<MyResponse> updateClient(
            @PathVariable Long clientId,
            @RequestBody @Valid ClientUpdateRequest updateRequest,
            @RequestHeader("Authorization") String bearerToken) {
        MyResponse response = clientService.updateClient(clientId, updateRequest, bearerToken);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * ID-based Endpoints
     */

    // Endpoint to delete a client
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/api/v1/client/{id}")
    public ResponseEntity<?> deleteClient(
            @PathVariable Long id,
            @RequestHeader("Authorization") String bearerToken) {

        MyResponse response = clientService.deleteClient(id, bearerToken);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Get client by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/client/{id}")
    public ResponseEntity<MyResponse> getClientById(@PathVariable Long id) {
        MyResponse response = clientService.getClientById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
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

    // get count of clients active and inactive
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/statistics/account/")
    public ResponseEntity<MyResponse> getActiveAndInactiveClientCount() {
        MyResponse response = clientService.getActiveAndInactiveClientCount();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

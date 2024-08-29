package com.idirtrack.backend.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // Get list of clients to use in select dropdown
    @GetMapping("/dropdown")
    public ResponseEntity<MyResponse> getClientsDropdown() {
        MyResponse response = clientService.getClientsDropdown();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
}

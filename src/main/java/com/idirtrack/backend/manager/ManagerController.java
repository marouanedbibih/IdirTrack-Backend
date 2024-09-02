package com.idirtrack.backend.manager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.manager.dtos.ManagerRequest;
import com.idirtrack.backend.manager.dtos.UpdateManagerRequest;
import com.idirtrack.backend.utils.MyResponse;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // Endpoint to delete a manager
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/api/v1/managers/{id}/")
    public ResponseEntity<MyResponse> deleteManager(
            @PathVariable Long id,
            @RequestHeader("Authorization") String barearToken) {
        MyResponse response = managerService.deleteManager(id, barearToken);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to get list of managers
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/v1/managers")
    public ResponseEntity<MyResponse> getAllManagers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        MyResponse response = managerService.getListOfManagers(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to get a manager by ID
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/v1/manager/{id}")
    public ResponseEntity<MyResponse> getManagerById(@PathVariable Long id) {
        MyResponse response = managerService.getManagerById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to update a manager
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/api/v1/manager/{id}")
    public ResponseEntity<MyResponse> updateManager(
            @RequestBody @Valid UpdateManagerRequest managerRequest,
            @PathVariable Long id,
            @RequestHeader("Authorization") String bearerToken) {
        MyResponse response = managerService.updateManager(managerRequest, id, bearerToken);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to create a manager
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/api/v1/manager")
    public ResponseEntity<MyResponse> createManager(
            @RequestBody @Valid ManagerRequest managerRequest,
            @RequestHeader("Authorization") String bearerToken) {

        MyResponse response = managerService.createManager(managerRequest, bearerToken);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

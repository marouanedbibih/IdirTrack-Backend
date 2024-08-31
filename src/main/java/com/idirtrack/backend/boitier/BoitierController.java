package com.idirtrack.backend.boitier;

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

import com.idirtrack.backend.boitier.https.BoitierRequest;
import com.idirtrack.backend.errors.DateException;
import com.idirtrack.backend.errors.NotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.RequestValidation;

@RestController
@RequestMapping("/api/boitier")
@RequiredArgsConstructor
public class BoitierController {

    private final BoitierService boitierService;

    // Endpoint to create a new boitier
    @PostMapping("/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> createBoitier(@Valid @RequestBody BoitierRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = boitierService.createNewBoitier(request);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (DateException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            }
        }
    }

    // Endpoint to update a boitier by ID
    @PutMapping("/{id}/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> updateBoitier(@PathVariable Long id, @Valid @RequestBody BoitierRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = boitierService.updateBoitier(id, request);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (DateException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            }
        }
    }

    // Endpoint to get boitier by ID
    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> getBoitier(@PathVariable Long id) {
        try {
            MyResponse response = boitierService.getBoitierById(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
        }
    }

    // Endpoint to get list of unassigned boitiers
    @GetMapping("/unassigned/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> getUnassignedBoitiers() {
        MyResponse response = boitierService.getUnassignedBoitiers();
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to delete boitier by ID
    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> deleteBoitier(
        @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam(required = true, defaultValue = "false") boolean isLost) {
        try {
            MyResponse response = boitierService.deleteBoitierById(id, isLost,authHeader);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
        }
    }
}

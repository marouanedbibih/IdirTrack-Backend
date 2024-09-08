package com.idirtrack.backend.vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.RequestValidation;
import com.idirtrack.backend.vehicle.https.UpdateVehicleRequest;
import com.idirtrack.backend.vehicle.https.VehicleRequest;

import jakarta.validation.Valid;

@RestController
public class VehcileController {

    @Autowired
    private VehicleService vehicleService;

    // Endpoint to search for a vehicle
    @GetMapping("/api/vehicles/search")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> searchVehicle(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            MyResponse response = vehicleService.searchVehicle(search, page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                    .message(ex.getMessage())
                    .build());
        }
    }

    // Endpoint to delete a vehicle
    @DeleteMapping("/api/vehicles/{vehicleId}/")
    public ResponseEntity<?> deleteVehicle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "false") boolean isLost) {
        try {
            MyResponse response = vehicleService.deleteVehicle(vehicleId, isLost, authHeader);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                    .message(ex.getMessage())
                    .build());
        }
    }

    // Update a vehicle
    @PutMapping("/api/vehicles/{vehicleId}/")
    public ResponseEntity<?> updateVehicle(@PathVariable Long vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request,
            @RequestHeader("Authorization") String authHeader,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = vehicleService.updateVehicle(vehicleId, request, authHeader);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (AlreadyExistException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (MyException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                        .message(ex.getMessage())
                        .build());
            }
        }
    }

    // Create a new vehicle
    @PostMapping("/api/vehicles/")
    public ResponseEntity<?> createNewVehicle(
            @Valid @RequestBody VehicleRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader) {
        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = vehicleService.createNewVehicle(request, authHeader);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (AlreadyExistException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (MyException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                        .message(ex.getMessage())
                        .build());
            }
        }

    }

    // Get all vehicles
    @GetMapping("/api/vehicles/")
    public ResponseEntity<?> getAllVehicles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            MyResponse response = vehicleService.getAllVehicles(page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                    .message(ex.getMessage())
                    .build());
        }
    }

    // Get a vehicle by ID
    @GetMapping("/api/vehicles/{vehicleId}/")
    public ResponseEntity<MyResponse> getVehicleById(@PathVariable Long vehicleId) {

        MyResponse response = vehicleService.getVehicleById(vehicleId);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Get a vehicle's boities
    @GetMapping("/api/vehicles/{vehicleId}/boities/")
    public ResponseEntity<?> getVehicleBoities(@PathVariable Long vehicleId) {
        try {
            MyResponse response = vehicleService.getVehicleBoities(vehicleId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }
}

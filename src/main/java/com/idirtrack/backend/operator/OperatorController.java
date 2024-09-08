package com.idirtrack.backend.operator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.MyResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    // Endpoint to create a new Operator
    @PostMapping("/api/v1/operator")
    public ResponseEntity<MyResponse> createOperator(
            @Valid @RequestBody OperatorRequest request)
            throws AlreadyExistException {
        // Call Service
        MyResponse response = operatorService.createOperator(request);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to update an existing Operator
    @PutMapping("/api/v1/operator/{id}")
    public ResponseEntity<MyResponse> updateSimType(
            @PathVariable Long id,
            @Valid @RequestBody OperatorRequest request)
            throws NotFoundException, AlreadyExistException {
        // Call Service
        MyResponse response = operatorService.updateOperator(id, request);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to delete an existing Operator
    @DeleteMapping("/api/v1/operator/{id}")
    public ResponseEntity<MyResponse> deleteOperator(@PathVariable Long id) throws NotFoundException {
        MyResponse response = operatorService.deleteOperatorById(id);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to get all Operators
    @GetMapping("/api/v1/operators/all")
    public ResponseEntity<MyResponse> getAllOperators() {
        MyResponse response = operatorService.getAllOperators();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to get an Operator by ID
    @GetMapping("/api/v1/operator/{id}")
    public ResponseEntity<MyResponse> getOperatorById(@PathVariable Long id) throws NotFoundException {
        MyResponse response = operatorService.getOperatorById(id);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to get Operators with pagination
    @GetMapping("/api/v1/operators")
    public ResponseEntity<MyResponse> getOperatorWithPagination(
            // Parameters
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        // Call Service
        MyResponse response = operatorService.getOperatorsWithPagination(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

}

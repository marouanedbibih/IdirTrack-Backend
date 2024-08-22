package com.idirtrack.backend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;



import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestValidation {

    public static ResponseEntity<ErrorResponse> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldErrorDTO> errors = extractErrorsFromBindingResult(bindingResult);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .fieldErrors(errors)
                            .build());
        }
        return null;
    }

    /**
     * Extarct the error message from the BindingResult
     * and return List of Error objects
     */
    public static List<FieldErrorDTO> extractErrorsFromBindingResult(BindingResult bindingResult) {
        // Declare a map to hold the first error for each field
        Map<String, FieldErrorDTO> errorsMap = new LinkedHashMap<>();

        // If there are errors in the BindingResult
        if (bindingResult.hasErrors()) {
            // Iterate over the FieldErrors
            for (FieldError error : bindingResult.getFieldErrors()) {
                // Add only the first error for each field
                errorsMap.putIfAbsent(error.getField(), FieldErrorDTO.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build());
            }
        }

        // Return the list of Error instances (only the first error per field)
        return new ArrayList<>(errorsMap.values());
    }
}

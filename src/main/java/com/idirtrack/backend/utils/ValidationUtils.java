package com.idirtrack.backend.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.idirtrack.backend.basics.BasicError;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;

public class ValidationUtils {

    public static ResponseEntity<BasicResponse> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<BasicError> errors = extractErrorsFromBindingResult(bindingResult);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BasicResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .messageType(MessageType.ERROR)
                            .errors(errors)
                            .build());
        }
        return null;
    }

    /**
     * Extarct the error message from the BindingResult
     * and return List of Error objects
     */
    public static List<BasicError> extractErrorsFromBindingResult(BindingResult bindingResult) {
        // Declare a map to hold the first error for each field
        Map<String, BasicError> errorsMap = new LinkedHashMap<>();

        // If there are errors in the BindingResult
        if (bindingResult.hasErrors()) {
            // Iterate over the FieldErrors
            for (FieldError error : bindingResult.getFieldErrors()) {
                // Add only the first error for each field
                errorsMap.putIfAbsent(error.getField(), BasicError.builder()
                        .key(error.getField())
                        .message(error.getDefaultMessage())
                        .build());
            }
        }

        // Return the list of Error instances (only the first error per field)
        return new ArrayList<>(errorsMap.values());
    }
}

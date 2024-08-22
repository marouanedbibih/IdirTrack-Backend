package com.idirtrack.backend.utils;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    // Errors
    private String message;
    private List<FieldErrorDTO> fieldErrors;

    private HttpStatus status;
}

package com.idirtrack.backend.errors;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

        // Handle AlreadyExistException
        @ExceptionHandler(AlreadyExistException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex) {
                return new ResponseEntity<>(ex.getResponse(), HttpStatus.CONFLICT);
        }

        // Handle NotFoundException
        @ExceptionHandler(NotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
                return new ResponseEntity<>(ex.getResponse(), HttpStatus.NOT_FOUND);
        }

        // Handle MyException
        @ExceptionHandler(MyException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ErrorResponse> handleMyException(MyException ex) {
                return new ResponseEntity<>(ex.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Handle other exceptions (generic)
        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .message("An unexpected error occurred: " + ex.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Handle validation errors
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
                List<FieldErrorDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .map(this::mapFieldError)
                                .collect(Collectors.toList());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .message("Validation failed")
                                .status(HttpStatus.BAD_REQUEST)
                                .fieldErrors(fieldErrors)
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Helper method to map field errors
        private FieldErrorDTO mapFieldError(FieldError fieldError) {
                return FieldErrorDTO.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .build();
        }
}

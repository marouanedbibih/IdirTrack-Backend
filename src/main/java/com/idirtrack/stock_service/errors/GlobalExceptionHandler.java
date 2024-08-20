package com.idirtrack.stock_service.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.idirtrack.stock_service.basics.BasicException;
import com.idirtrack.stock_service.basics.BasicResponse;
import com.idirtrack.stock_service.utils.ErrorResponse;
import com.idirtrack.stock_service.utils.FieldErrorDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex)
                        throws BasicException {
                BindingResult bindingResult = ex.getBindingResult();

                // Capture only the first error for each field
                Map<String, String> fieldErrorsMap = bindingResult.getFieldErrors().stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                FieldError::getDefaultMessage,
                                                (firstError, secondError) -> firstError // Keep the first error and
                                                                                        // ignore the rest
                                ));

                // Convert the map to a list of FieldErrorDTO
                List<FieldErrorDTO> fieldErrors = fieldErrorsMap.entrySet().stream()
                                .map(entry -> new FieldErrorDTO(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());

                // Create the error response
                ErrorResponse response = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .fieldErrors(fieldErrors)
                                .build();

                // Throw BasicException with the constructed ErrorResponse
                throw new BasicException(response);
        }


        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
                return new ResponseEntity<>(ex.getResponse(), ex.getResponse().getStatus());
        }

        @ExceptionHandler(AlreadyExistException.class)
        public ResponseEntity<ErrorResponse> handleExistException(AlreadyExistException ex) {
                return new ResponseEntity<>(ex.getResponse(), ex.getResponse().getStatus());
        }
}

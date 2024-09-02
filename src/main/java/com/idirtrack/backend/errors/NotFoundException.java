package com.idirtrack.backend.errors;

import com.idirtrack.backend.utils.ErrorResponse;

public class NotFoundException extends RuntimeException {
    private ErrorResponse response;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(ErrorResponse response) {
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }

}

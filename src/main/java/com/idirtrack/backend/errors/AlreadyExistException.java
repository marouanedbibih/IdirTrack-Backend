package com.idirtrack.backend.errors;

import com.idirtrack.backend.utils.ErrorResponse;

public class AlreadyExistException extends Exception {
    private ErrorResponse response;

    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException(ErrorResponse response) {
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}

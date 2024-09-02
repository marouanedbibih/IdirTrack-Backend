package com.idirtrack.backend.errors;

import com.idirtrack.backend.utils.ErrorResponse;

public class DateException extends RuntimeException {
    private ErrorResponse response;

    public DateException(String message) {
        super(message);
    }

    public DateException(ErrorResponse response) {
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}

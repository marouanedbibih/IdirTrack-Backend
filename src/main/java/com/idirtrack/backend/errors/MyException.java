package com.idirtrack.backend.errors;

import com.idirtrack.backend.utils.ErrorResponse;

public class MyException extends RuntimeException {
    private ErrorResponse response;

    public MyException(String message) {
        super(message);
    }

    public MyException(ErrorResponse response) {
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}

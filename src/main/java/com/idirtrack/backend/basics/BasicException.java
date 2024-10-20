package com.idirtrack.backend.basics;

import org.springframework.http.ResponseCookie;

import com.idirtrack.backend.utils.ErrorResponse;

public class BasicException extends Exception {
  private BasicResponse response;
  private ErrorResponse errorResponse;

  // Basic Response
  public BasicException(BasicResponse response) {
    super(response.getMessage());
    this.response = response;
  }

  // Error Response
  public BasicException(ErrorResponse errorResponse) {
    super(errorResponse.getMessage());
    this.errorResponse = errorResponse;
  }

  // Get Message
  public BasicException(String message) {
    super(message);
  }

  // Get Response
  public BasicResponse getResponse() {
    return response;
  }

  // Get Error Response
  public ErrorResponse getErrorResponse() {
    return errorResponse;
  }

  // Set Response cookie
   public ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie.from("JSESSIONID", sessionId)
                .httpOnly(true)
                .path("/")
                .build();
    }
}

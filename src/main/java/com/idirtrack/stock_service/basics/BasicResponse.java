package com.idirtrack.stock_service.basics;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicResponse {
    private Object content;
    private String message;
    private Map<String, String> messagesObject;
    private MessageType messageType;
    private String redirectUrl;
    private HttpStatus status;
    private MetaData metadata;

    private BasicError error;
    private List<BasicError> errors;
}

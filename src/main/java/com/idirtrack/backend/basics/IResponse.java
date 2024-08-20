package com.idirtrack.backend.basics;

import org.springframework.http.HttpStatus;

public interface IResponse {
    
        public org.springframework.http.HttpStatus getStatus();
    
        public MessageType getMessageType();
    
        public String getMessage();
    
        public Object getData();
    
        public void setStatus(HttpStatus status);
    
        public void setMessageType(MessageType messageType);
    
        public void setMessage(String message);
    
        public void setData(Object data);
}

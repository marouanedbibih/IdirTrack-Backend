package com.idirtrack.backend.traccar.request;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TracCarDeviceRequest {
    private String name;
    private String uniqueId;
    private String phone;
    private  Date expirationTime;
}

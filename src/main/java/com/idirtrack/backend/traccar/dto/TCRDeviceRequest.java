package com.idirtrack.backend.traccar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TCRDeviceRequest {
    private String clientName;
    private String clientCompany;
    private String imei;
    private String vehicleMatricule;
    private String vehicleType;
    private Long traccarId;
}

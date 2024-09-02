package com.idirtrack.backend.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientTableDTO {
    // IDs
    private Long clientId;
    private Long userId;

    // Client Info
    private String username;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String cne;
    private String categoryName;
    private String remarque;
    private boolean isDisabled;

    // Vehicles
    private Integer totalVehicles;



    
}

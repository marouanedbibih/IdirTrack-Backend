package com.idirtrack.backend.staff;
import com.idirtrack.backend.client.dtos.ClientDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long id;
    private String name;
    private String phone;
    private String position;

    // Client Informations
    private Long clientId;
    private String clientName;
    private String clientCompany;
    private ClientDTO client;
}

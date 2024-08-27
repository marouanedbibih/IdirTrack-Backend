package com.idirtrack.backend.vehicle.https;

import java.util.List;

import com.idirtrack.backend.boitier.dto.BoitierDTO;
import com.idirtrack.backend.client.ClientDTO;
import com.idirtrack.backend.vehicle.VehicleDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {

    private VehicleDTO vehicle;
    private ClientDTO client;
    private List<BoitierDTO> boitiersList;
}

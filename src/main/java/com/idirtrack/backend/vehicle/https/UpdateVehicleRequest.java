package com.idirtrack.backend.vehicle.https;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVehicleRequest {
    @NotBlank(message = "Matricule is required")
    private String matricule;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "You need to select client")
    private Long clientId;

}

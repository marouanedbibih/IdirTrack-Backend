package com.idirtrack.backend.boitier.https;

import java.sql.Date;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoitierRequest {
    // Device Informations
    @NotNull(message = "Stock Microservice ID is required")
    private Long deviceId;

    // Card Sim Informations
    @NotNull(message = "Stock Microservice ID is required")
    private Long simId;

    // Subscription Informations
    @NotNull(message = "Start date is required")
    private Date startDate;

    @NotNull(message = "End date is required")
    private Date endDate;
}

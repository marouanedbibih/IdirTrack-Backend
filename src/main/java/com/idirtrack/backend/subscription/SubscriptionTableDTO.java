package com.idirtrack.backend.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionTableDTO {

    private Long id;
    private Long boitierId;
    private String imei;
    private String phone;
    private String matricule;
    private String clientName;
    private String startDate;
    private String endDate;
    private String timeLeft;
    private String status;
}

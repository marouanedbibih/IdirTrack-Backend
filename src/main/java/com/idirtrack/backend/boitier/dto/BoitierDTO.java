package com.idirtrack.backend.boitier.dto;
import java.util.List;

import com.idirtrack.backend.device.DeviceDTO;
import com.idirtrack.backend.sim.SimDTO;
import com.idirtrack.backend.subscription.SubscriptionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoitierDTO {
    private Long id;
    private DeviceDTO device;
    private SimDTO sim;
    private SubscriptionDTO subscription;
    // private VehicleDTO vehicle;
    private List<SubscriptionDTO> subscriptionsList;

}

package com.vena.usageservice.modal;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEnergy {
    private Long deviceId;
    private Double energyConsumed;
    // Use boxed Long so we can represent "unknown" userId as null
    private Long userId;
}

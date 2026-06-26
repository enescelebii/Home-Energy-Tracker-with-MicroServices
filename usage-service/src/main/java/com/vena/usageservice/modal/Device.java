package com.vena.usageservice.modal;


import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    Long id;
    String name;
    String type;
    String location;
    Long userId;
    Double energyConsumed;
}

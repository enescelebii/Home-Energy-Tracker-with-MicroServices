package com.vena.alertservice.dto;


import lombok.Builder;

@Builder
public record UserDto (
        Long id,
        String name,
        String surname,
        String email,
        String address,
        boolean alerting,
        Double energyAlertingThreshold
){
}

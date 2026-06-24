package com.vena.kafka.event;

import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertingEvent
{
    Long userId;
    String message;
    Double threshold;
    Double energyConsumed;
    String email;
}

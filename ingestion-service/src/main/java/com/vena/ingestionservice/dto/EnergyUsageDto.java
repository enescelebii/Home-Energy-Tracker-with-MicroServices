package com.vena.ingestionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.time.Instant;

@Builder
public record EnergyUsageDto (
    Long deviceId,
    Double energyConsumed,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp
){}

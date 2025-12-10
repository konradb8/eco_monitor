package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ChargeWindowResponse(
        @JsonProperty("from")
        LocalDateTime from,
        @JsonProperty("to")
        LocalDateTime to,
        @JsonProperty("cleanEnergyPerc")
        double cleanEnergyPerc
) {
}

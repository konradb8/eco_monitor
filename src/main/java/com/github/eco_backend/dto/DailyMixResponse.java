package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

public record DailyMixResponse(
        @JsonProperty("date")
        LocalDate date,
        @JsonProperty("fuelPerc")
        Map<String, Double> fuelPercentage,
        @JsonProperty("cleanEnergyPerc")
        double cleanEnergyPercentage
) {
}
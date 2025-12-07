package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationMixResponse(
        @JsonProperty("fuel") String fuel,
        @JsonProperty("perc") Double percentage
) {
}
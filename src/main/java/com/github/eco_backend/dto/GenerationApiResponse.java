package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationApiResponse(
        @JsonProperty("data")
        List<GenerationDataResponse> data
) {
}

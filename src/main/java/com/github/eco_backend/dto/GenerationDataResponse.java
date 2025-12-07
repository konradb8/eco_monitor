package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationDataResponse(
        @JsonProperty("from") String from,
        @JsonProperty("to") String to,
        @JsonProperty("generationmix") List<GenerationMixResponse> generationMixResponses
) {
}

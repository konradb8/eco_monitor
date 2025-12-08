package com.github.eco_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record ErrorResponse(

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        OffsetDateTime timestamp,
        int status,
        String errorMessage,
        String message,
        String path
) {
}

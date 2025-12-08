package com.github.eco_backend.dto;

import java.time.LocalDateTime;

public record ChargeWindowResponse(
        LocalDateTime from,
        LocalDateTime to,
        double cleanEnergyPerc
) {
}

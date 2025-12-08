package com.github.eco_backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CleanEnergyInterval {

    LocalDateTime startTime;

    double cleanEnergyPerc;

}

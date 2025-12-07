package com.github.eco_backend.controller;

import com.github.eco_backend.dto.DailyMixResponse;
import com.github.eco_backend.service.EnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/")
public class EnergyMixController {

    private final EnergyService energyService;

    @GetMapping("mix-forecast")
    ResponseEntity<List<DailyMixResponse>> getEnergyMixFromApi() {
        List<DailyMixResponse> response = energyService.getAverageEnergyMix();

        return ResponseEntity.ok(response);
    }
}

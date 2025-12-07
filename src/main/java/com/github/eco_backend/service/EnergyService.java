package com.github.eco_backend.service;

import com.github.eco_backend.dto.DailyMixResponse;
import com.github.eco_backend.dto.GenerationApiResponse;
import com.github.eco_backend.dto.GenerationDataResponse;
import com.github.eco_backend.dto.GenerationMixResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EnergyService {

    private final ExternalApiService externalApiService;

    private static final Set<String> CLEAN_SOURCES = Set.of(
            "solar", "wind", "hydro", "nuclear", "biomass"
    );

    public List<DailyMixResponse> getAverageEnergyMix() {
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));

        LocalDateTime start = today.atStartOfDay();

        LocalDateTime end = start.plusDays(3);

        GenerationApiResponse generationApiResponse = externalApiService.fetchGenerationData(start, end);

        return generationApiResponse.data().stream()
                .collect(Collectors.groupingBy(
                        item -> ZonedDateTime.parse(item.from())
                                .toLocalDate()
                ))
                .entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(today))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> calculateDailyAverage(entry.getKey(), entry.getValue()))
                .toList();
    }

    private DailyMixResponse calculateDailyAverage(LocalDate date, List<GenerationDataResponse> dataList) {
        Map<String, Double> averageMix = dataList.stream()
                .flatMap(interval -> interval.generationMixResponses().stream())
                .collect(Collectors.groupingBy(
                        GenerationMixResponse::fuel,
                        Collectors.averagingDouble(GenerationMixResponse::percentage)
                ));

        double cleanEnergyPercent = averageMix.entrySet().stream()
                .filter(entry -> CLEAN_SOURCES.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        return new DailyMixResponse(date, averageMix, cleanEnergyPercent);
    }

}


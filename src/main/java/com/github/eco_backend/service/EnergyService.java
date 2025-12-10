package com.github.eco_backend.service;

import com.github.eco_backend.dto.ChargeWindowResponse;
import com.github.eco_backend.dto.DailyMixResponse;
import com.github.eco_backend.dto.GenerationApiResponse;
import com.github.eco_backend.dto.GenerationDataResponse;
import com.github.eco_backend.dto.GenerationMixResponse;
import com.github.eco_backend.model.CleanEnergyInterval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class EnergyService {

    private final ExternalApiService externalApiService;

    private static final Set<String> CLEAN_SOURCES = Set.of(
            "solar", "wind", "hydro", "nuclear", "biomass"
    );

    public List<DailyMixResponse> getAverageEnergyMix() {
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));

        Instant start = today.atStartOfDay(ZoneId.of("UTC")).toInstant();

        Instant end = start.plus(3, ChronoUnit.DAYS);

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

    public ChargeWindowResponse getOptimalChargeWindow(int duration) {

        if (duration > 6 || duration < 1) {
            throw new IllegalArgumentException("Duration must be between 1 and 6 hours");
        }

        ZonedDateTime nowUTC = LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC"));
        Instant start = nowUTC.toInstant();

        Instant end = start.plus(2, ChronoUnit.DAYS);
        log.info("Window start: {}, Window end: {}", start, end);
        GenerationApiResponse generationApiResponse = externalApiService.fetchGenerationData(start, end);

        List<CleanEnergyInterval> intervals = generationApiResponse.data().stream()
                .map(item -> new CleanEnergyInterval(
                        ZonedDateTime.parse(item.from()).toLocalDateTime(),
                        calculateCleanEnergyForInterval(item.generationMixResponses())
                ))
                .sorted(Comparator.comparing(CleanEnergyInterval::getStartTime))
                .toList();

        int slotsNeeded = duration * 2;

        if (intervals.size() < slotsNeeded) {
            throw new IllegalStateException("Lack of data to calculate energy mix");
        }

        return IntStream.rangeClosed(0, intervals.size() - slotsNeeded)
                .mapToObj(i -> {
                    List<CleanEnergyInterval> chargeWindows = intervals.subList(i, i + slotsNeeded);

                    double average = chargeWindows.stream()
                            .mapToDouble(CleanEnergyInterval::getCleanEnergyPerc)
                            .average()
                            .orElse(0.0);

                    return new ChargeWindowResponse(
                            chargeWindows.getFirst().getStartTime(),
                            chargeWindows.getFirst().getStartTime().plusHours(duration),
                            average
                    );
                })
                .max(Comparator.comparingDouble(ChargeWindowResponse::cleanEnergyPerc))
                .orElseThrow(() -> new IllegalStateException("Error occurred while getting optimal charge window"));

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

    private double calculateCleanEnergyForInterval(List<GenerationMixResponse> mix) {
        return mix.stream()
                .filter(entry -> CLEAN_SOURCES.contains(entry.fuel()))
                .mapToDouble(GenerationMixResponse::percentage)
                .sum();
    }

}


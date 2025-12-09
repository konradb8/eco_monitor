package com.github.eco_backend.service;

import com.github.eco_backend.client.GenerationDataClient;
import com.github.eco_backend.dto.DailyMixResponse;
import com.github.eco_backend.dto.GenerationApiResponse;
import com.github.eco_backend.dto.GenerationDataResponse;
import com.github.eco_backend.dto.GenerationMixResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private GenerationDataClient generationDataClient;

    @Mock
    private ExternalApiService externalApiService;

    @InjectMocks
    private EnergyService underTest;

    @Test
    void testGetAverageEnergyMix() {

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate tomorrow = today.plusDays(1);
        LocalDate tdatomorrow = today.plusDays(2);
        LocalDate yesterday = today.minusDays(1);

        ZonedDateTime todayTime = today.atTime(12, 0).atZone(ZoneId.of("UTC"));
        ZonedDateTime tomorrowTime = tomorrow.atTime(12, 0).atZone(ZoneId.of("UTC"));
        ZonedDateTime yesterdayTime = yesterday.atTime(12, 0).atZone(ZoneId.of("UTC"));
        ZonedDateTime tdatomorrowTime = tdatomorrow.atTime(12, 0).atZone(ZoneId.of("UTC"));

        List<GenerationMixResponse> mixA = createMixList(35.5, 25.0, 15.0, 10.0, 14.5);
        List<GenerationMixResponse> mixB = createMixList(25.5, 22.0, 19.0, 21.0, 10.5);
        List<GenerationMixResponse> mixC = createMixList(30.7, 15.0, 25.0, 16.0, 20.4);

        List<GenerationDataResponse> mockData = List.of(
                createDataEntry(yesterdayTime, mixA),
                createDataEntry(todayTime, mixB),
                createDataEntry(todayTime.plusHours(2), mixC),
                createDataEntry(tomorrowTime, mixA),
                createDataEntry(tomorrowTime.plusHours(2), mixB),
                createDataEntry(tdatomorrowTime, mixC),
                createDataEntry(tdatomorrowTime.plusHours(2), mixA)
        );

        Map<String, Double> expectedFuelMixToday = createExpectedFuelMixMap(mixB, mixC);
        Map<String, Double> expectedFuelMixTomorrow = createExpectedFuelMixMap(mixA, mixB);
        Map<String, Double> expectedFuelMixTdatomorrow = createExpectedFuelMixMap(mixC, mixA);

        double expectedCleanPercentageToday = 65.1;
        double expectedCleanPercentageTomorrow = 69.5;
        double expectedCleanPercentageTdatomorrow = 66.1;

        GenerationApiResponse mockResponse = new GenerationApiResponse(mockData);

        when(externalApiService.fetchGenerationData(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        List<DailyMixResponse> result = underTest.getAverageEnergyMix();

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .extracting(
                        DailyMixResponse::date,
                        DailyMixResponse::fuelPercentage,
                        DailyMixResponse::cleanEnergyPercentage
                )
                .containsExactly(
                        tuple(
                                today,
                                expectedFuelMixToday,
                                expectedCleanPercentageToday
                        ),
                        tuple(
                                tomorrow,
                                expectedFuelMixTomorrow,
                                expectedCleanPercentageTomorrow
                        ),
                        tuple(
                                tdatomorrow,
                                expectedFuelMixTdatomorrow,
                                expectedCleanPercentageTdatomorrow
                        )
                );

        verify(externalApiService).fetchGenerationData(any(), any());
        verifyNoMoreInteractions(externalApiService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 7, 10, 100})
    void testGetOptimalChargeWindow_windowLengthIncorrect(int duration) {

        assertThatThrownBy(() -> underTest.getOptimalChargeWindow(duration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duration must be between 1 and 6 hours");

    }

    private GenerationMixResponse createMix(String fuel, double percentage) {
        return new GenerationMixResponse(fuel, percentage);
    }

    private List<GenerationMixResponse> createMixList(double solar, double wind, double gas, double nuclear, double coal) {
        return List.of(
                createMix("solar", solar),
                createMix("wind", wind),
                createMix("gas", gas),
                createMix("nuclear", nuclear),
                createMix("coal", coal)
        );
    }

    @SafeVarargs
    private Map<String, Double> createExpectedFuelMixMap(List<GenerationMixResponse>... mixLists) {
        if (mixLists == null || mixLists.length == 0) {
            return Map.of();
        }

        return Arrays.stream(mixLists)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        GenerationMixResponse::fuel,
                        Collectors.averagingDouble(GenerationMixResponse::percentage)
                ));
    }

    private GenerationDataResponse createDataEntry(ZonedDateTime startTime, List<GenerationMixResponse> mix) {
        return new GenerationDataResponse(
                startTime.toString(),
                startTime.plusHours(1).toString(),
                mix
        );
    }
}

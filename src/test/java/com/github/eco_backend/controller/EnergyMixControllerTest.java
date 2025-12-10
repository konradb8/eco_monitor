package com.github.eco_backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.eco_backend.dto.ChargeWindowResponse;
import com.github.eco_backend.dto.DailyMixResponse;
import com.github.eco_backend.service.EnergyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = EnergyMixController.class)
class EnergyMixControllerTest {

    private static final LocalDateTime TEST_DATE_TIME = LocalDateTime.of(
            2025, 12, 5, 14, 0, 30
    );
    private static final LocalDateTime TEST_DATE_TIME2 = LocalDateTime.of(
            2025, 12, 7, 10, 0, 40
    );
    private static final LocalDate TEST_DATE = LocalDate.of(
            2025, 12, 7
    );

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnergyService energyService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("")
    void whenGetAverageEnergyMix_thenReturnOk() throws Exception {

        List<DailyMixResponse> response = List.of(
                new DailyMixResponse(TEST_DATE, Map.of("wind", 20.0), 60),
                new DailyMixResponse(TEST_DATE.plusDays(1), Map.of("solar", 10.0), 55),
                new DailyMixResponse(TEST_DATE.plusDays(2), Map.of("coal", 3.0), 65)
        );

        mockMvc.perform(get("/api/v1/mix-forecast")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isOk());

        verify(energyService).getAverageEnergyMix();
    }

    @Test
    @DisplayName("")
    void whenDurationIsValid_thenReturnOk() throws Exception {

        ChargeWindowResponse response = new ChargeWindowResponse(TEST_DATE_TIME, TEST_DATE_TIME2, 75.0);

        mockMvc.perform(get("/api/v1/charge-window")
                        .param("duration", "5")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isOk());

        verify(energyService).getOptimalChargeWindow(5);
    }

}

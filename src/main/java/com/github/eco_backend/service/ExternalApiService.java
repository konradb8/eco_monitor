package com.github.eco_backend.service;

import com.github.eco_backend.client.GenerationDataClient;
import com.github.eco_backend.dto.GenerationApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final GenerationDataClient generationDataClient;

    public GenerationApiResponse fetchGenerationData(LocalDateTime from, LocalDateTime to) {

        return generationDataClient.getGenerationMixData(from.toString(), to.toString());
    }

}

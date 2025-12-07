package com.github.eco_backend.client;

import com.github.eco_backend.config.FeignConfig;
import com.github.eco_backend.dto.GenerationApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "energy-api",
        url = "${external.api.base-url}",
        configuration = FeignConfig.class)
public interface GenerationDataClient {

    @GetMapping("/generation/{from}/{to}")
    GenerationApiResponse getGenerationMixData(
            @PathVariable("from") String from,
            @PathVariable("to") String to
    );

}
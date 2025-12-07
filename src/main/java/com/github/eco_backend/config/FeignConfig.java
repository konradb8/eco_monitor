package com.github.eco_backend.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String url = requestTemplate.url();
            if (url.contains("%3A")) {
                String decodedUrl = url.replace("%3A", ":");
                requestTemplate.uri(decodedUrl);
            }
        };
    }
}

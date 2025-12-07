package com.github.eco_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoBackendApplication.class, args);
    }

}

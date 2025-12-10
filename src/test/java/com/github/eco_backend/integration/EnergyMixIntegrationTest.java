package com.github.eco_backend.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EnergyMixIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeAll
     static void setUrl() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1/";
    }

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Should return 200 OK and a list of 3 daily energy mix forecasts")
    void getMixForecast_thenReturnBodyAndStatusOK() {
        RestAssured.given()
                .when()
                .get("mix-forecast")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("$", hasSize(3));

    }

    @Test
    @DisplayName("Should return 200 OK and charge window details")
    void getChargeWindow_thenReturnBodyAndStatusOK() {
        RestAssured.given()
                .param("duration", 3)
                .when()
                .get("charge-window")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("from", is(notNullValue()))
                .body("to", is(notNullValue()))
                .body("cleanEnergyPerc", is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when duration is invalid")
    void getChargeWindow_DurationInvalid_thenReturnBodyAndStatusBadReqeust() {
        RestAssured.given()
                .param("duration", 35)
                .when()
                .get("charge-window")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
                .body("status", is(HttpStatus.BAD_REQUEST.value()))
                .body("errorMessage", is("BAD_REQUEST"))
                .body("message", is("Duration must be between 1 and 6 hours"))
                .body("path", is("/api/v1/charge-window"));
    }
}

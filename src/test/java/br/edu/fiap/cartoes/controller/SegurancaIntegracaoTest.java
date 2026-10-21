package br.edu.fiap.cartoes.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class SegurancaIntegracaoTest {

    @Test
    void deveRetornar401AoListarCartoesSemToken() {
        given()
            .when()
            .get("/api/cartoes")
            .then()
            .statusCode(401);
    }
}

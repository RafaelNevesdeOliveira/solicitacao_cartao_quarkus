package br.edu.fiap.cartoes.controller;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/** Cobertura mínima de segurança: rotas protegidas devem negar acesso sem JWT. */
@QuarkusTest
class SegurancaIntegracaoTest {

    @Test
    void tiposCartao_semToken_deveRetornar401() {
        given()
        .when().get("/api/tipos-cartao")
        .then()
            .statusCode(401)
            .header("WWW-Authenticate", is("Bearer"));
    }

    @Test
    void solicitacoes_semToken_deveRetornar401() {
        given()
        .when().get("/api/solicitacoes")
        .then()
            .statusCode(401)
            .header("WWW-Authenticate", is("Bearer"));
    }

    @Test
    void cartoes_semToken_deveRetornar401() {
        given()
        .when().get("/api/cartoes")
        .then()
            .statusCode(401)
            .header("WWW-Authenticate", is("Bearer"));
    }
}

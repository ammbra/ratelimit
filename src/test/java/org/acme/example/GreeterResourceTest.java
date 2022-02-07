package org.acme.example;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class GreeterResourceTest {

    @ConfigProperty(name = "requests.per.minute", defaultValue = "15")
    int customLimit;

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/greeting")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }

    @Test
    public void testLimitedEndpoint() {
        for (int i=0; i<customLimit; i++) {
            given()
                    .when().get("/greeting/random")
                    .then()
                    .statusCode(200).header("X-Rate-Limit-Remaining", String.valueOf((customLimit - i - 1)))
                    .body(notNullValue());
        }
        given()
                .when().get("/greeting/random")
                .then()
                .statusCode(429).header("X-Rate-Limit-Retry-After-Seconds", notNullValue())
                .body(notNullValue());
    }

}
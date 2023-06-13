package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

@QuarkusTest
public class BankAccountResourceTest {

    @Test
    public void testWorkflow() {

        // create account (201)
        String bankAccountId = given()
                .when()
                .body("{\"email\":\"test@test.com\", \"userName\":\"testuser12345\", \"address\":\"11 Test Lane, Test Town, TT1 1TT\"}")
                .contentType("application/json")
                .post("/api/v1/bank")
                .then()
                .statusCode(201)
                .body(notNullValue())
                .extract()
                .asString();

        // update email address (204)
        given()
                .when()
                .body("{\"email\":\"test123@test.com\"}")
                .contentType("application/json")
                .post("/api/v1/bank/email/" + bankAccountId)
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));

        // update address (204)
        given()
                .when()
                .body("{\"address\":\"64 Test Ave, Testington, 1TT TT1\"}")
                .contentType("application/json")
                .post("/api/v1/bank/address/" + bankAccountId)
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));

        // deposit (204)
        given()
                .when()
                .body("{\"amount\": 500.00}")
                .contentType("application/json")
                .post("/api/v1/bank/deposit/" + bankAccountId)
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));

        // withdrawal (204)
        given()
                .when()
                .body("{\"amount\": 100.00}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));

        // insufficient funds (400)
        given()
                .when()
                .body("{\"amount\": 5000.00}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400)
                .body(containsString("does not have sufficient funds for withdrawal"));

        // minimum withdrawal not met (400)
        given()
                .when()
                .body("{\"amount\": 1.00}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400)
                .body(containsString("minimum withdrawal is 10"));

        // account not found (404)
        given()
                .when()
                .body("{\"amount\": 100.00}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/bd566140-ec09-43cf-9296-de3992d181a8")
                .then()
                .statusCode(404)
                .body(containsString("bank account not found"));

        // bad request - invalid data (400)
        given()
                .when()
                .body("{\"amount\": \"abc\"}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400);

        // bad request - empty payload (400)
        given()
                .when()
                .body("{}")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400);

        // bad request - empty body (400)
        given()
                .when()
                .body("")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400);

        // bad request - non-json (400)
        given()
                .when()
                .body("abcd")
                .contentType("application/json")
                .post("/api/v1/bank/withdraw/" + bankAccountId)
                .then()
                .statusCode(400);
    }

}

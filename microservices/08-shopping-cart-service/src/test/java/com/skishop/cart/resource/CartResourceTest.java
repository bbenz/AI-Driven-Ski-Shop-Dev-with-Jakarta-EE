package com.skishop.cart.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class CartResourceTest {

    @Test
    public void testGetOrCreateCartBySession() {
        given()
            .when().get("/api/v1/carts/session/test-session-001")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("cartId", notNullValue())
                .body("sessionId", is("test-session-001"))
                .body("status", is("ACTIVE"));
    }

    @Test
    public void testAddItemToCart() {
        // First create a cart
        String response = given()
            .when().get("/api/v1/carts/session/test-session-002")
            .then()
                .statusCode(200)
                .extract().asString();
        
        // Extract cartId from response (simplified for test)
        String cartId = "cart-" + System.currentTimeMillis();
        
        // Add item to cart
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "productId": "550e8400-e29b-41d4-a716-446655440301",
                    "sku": "SKI-BOOT-TEST-001",
                    "productName": "Test Ski Boots",
                    "unitPrice": 15000.00,
                    "quantity": 1
                }
                """)
            .when().post("/api/v1/carts/" + cartId + "/items")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void testHealthCheck() {
        given()
            .when().get("/q/health/ready")
            .then()
                .statusCode(200);
    }

    @Test
    public void testMetricsEndpoint() {
        given()
            .when().get("/metrics")
            .then()
                .statusCode(200);
    }
}

package com.ski.shop.catalog;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Product Catalog Service API テスト
 */
@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }

    @Test
    public void testSearchProducts() {
        given()
          .when().get("/api/v1/products")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("size()", is(5)); // サンプルデータが5件
    }

    @Test
    public void testGetFeaturedProducts() {
        given()
          .when().get("/api/v1/products/featured")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("size()", is(3)); // 注目商品が3件
    }

    @Test
    public void testGetProductBySku() {
        given()
          .when().get("/api/v1/products/sku/ROX-CARV-165")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("sku", is("ROX-CARV-165"))
             .body("name", notNullValue())
             .body("category", notNullValue())
             .body("brand", notNullValue());
    }

    @Test
    public void testGetProductBySkuNotFound() {
        given()
          .when().get("/api/v1/products/sku/NOT-EXIST")
          .then()
             .statusCode(404);
    }

    @Test
    public void testOpenApiSpec() {
        given()
          .when().get("/q/openapi")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("openapi", notNullValue())
             .body("info.title", is("Product Catalog Service API"));
    }
}

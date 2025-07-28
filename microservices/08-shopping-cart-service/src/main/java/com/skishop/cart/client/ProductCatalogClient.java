package com.skishop.cart.client;

import com.skishop.cart.dto.external.ProductDetailDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * REST client for Product Catalog Service integration
 */
@Path("/api/v1/products")
@RegisterRestClient(configKey = "product-catalog-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProductCatalogClient {

    @GET
    @Path("/{productId}")
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    ProductDetailDto getProduct(@PathParam("productId") String productId);

    @POST
    @Path("/batch")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    List<ProductDetailDto> getProductsBatch(List<String> productIds);

    @POST
    @Path("/batch/async")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    CompletionStage<List<ProductDetailDto>> getProductsBatchAsync(List<String> productIds);

    @GET
    @Path("/health")
    @Timeout(5000)
    void healthCheck();
}

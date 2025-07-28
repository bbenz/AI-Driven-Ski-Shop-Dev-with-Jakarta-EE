package com.skishop.cart.service.integration;

import com.skishop.cart.client.ProductCatalogClient;
import com.skishop.cart.dto.external.ProductDetailDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Integration service for Product Catalog Service
 */
@ApplicationScoped
public class ProductCatalogIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogIntegrationService.class);
    
    @Inject
    @RestClient
    ProductCatalogClient productCatalogClient;
    
    @Fallback(fallbackMethod = "getProductDetailsFallback")
    public ProductDetailDto getProductDetails(String productId) {
        logger.debug("Fetching product details for productId: {}", productId);
        return productCatalogClient.getProduct(productId);
    }
    
    @Fallback(fallbackMethod = "getProductDetailsBatchFallback")
    public List<ProductDetailDto> getProductDetailsBatch(List<String> productIds) {
        logger.debug("Fetching batch product details for {} products", productIds.size());
        return productCatalogClient.getProductsBatch(productIds);
    }
    
    public CompletionStage<List<ProductDetailDto>> getProductDetailsBatchAsync(List<String> productIds) {
        logger.debug("Fetching async batch product details for {} products", productIds.size());
        return productCatalogClient.getProductsBatchAsync(productIds);
    }
    
    // Fallback methods
    public ProductDetailDto getProductDetailsFallback(String productId) {
        logger.warn("Using fallback for product details. ProductId: {}", productId);
        return new ProductDetailDto(
            productId,
            "UNKNOWN",
            "Product temporarily unavailable",
            "Product information is currently unavailable",
            BigDecimal.ZERO,
            "JPY",
            "UNKNOWN",
            "/images/placeholder.png",
            false,
            0,
            Collections.emptyMap(),
            false
        );
    }
    
    public List<ProductDetailDto> getProductDetailsBatchFallback(List<String> productIds) {
        logger.warn("Using fallback for batch product details. ProductIds: {}", productIds);
        return productIds.stream()
            .map(this::getProductDetailsFallback)
            .toList();
    }
}

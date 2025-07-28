package com.skishop.cart.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class ProductCatalogService {
    
    private static final Logger LOG = Logger.getLogger(ProductCatalogService.class);
    
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Fallback(fallbackMethod = "validateProductFallback")
    public void validateProduct(UUID productId, BigDecimal expectedPrice) {
        LOG.infof("Validating product: %s with price: %s", productId, expectedPrice);
        
        // TODO: Implement actual REST client call to product catalog service
        // For now, we'll simulate validation
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (expectedPrice == null || expectedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }
        
        LOG.debugf("Product validation successful for: %s", productId);
    }
    
    public void validateProductFallback(UUID productId, BigDecimal expectedPrice) {
        LOG.warnf("Product validation fallback for: %s - assuming valid", productId);
        // In fallback, we assume the product is valid to avoid blocking cart operations
    }
    
    @Retry(maxRetries = 2)
    @Fallback(fallbackMethod = "getProductDetailsFallback")
    public ProductDetails getProductDetails(UUID productId) {
        LOG.infof("Getting product details for: %s", productId);
        
        // TODO: Implement actual REST client call to product catalog service
        // For now, return mock data
        
        return new ProductDetails(
            productId,
            "SKU-" + productId.toString().substring(0, 8),
            "Mock Product",
            "Mock product description",
            new BigDecimal("1000.00"),
            "https://example.com/image.jpg",
            true
        );
    }
    
    public ProductDetails getProductDetailsFallback(UUID productId) {
        LOG.warnf("Product details fallback for: %s", productId);
        
        return new ProductDetails(
            productId,
            "SKU-FALLBACK",
            "Product (Unavailable)",
            "Product details unavailable",
            BigDecimal.ZERO,
            null,
            false
        );
    }
    
    // DTO for product details
    public record ProductDetails(
        UUID productId,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available
    ) {}
}

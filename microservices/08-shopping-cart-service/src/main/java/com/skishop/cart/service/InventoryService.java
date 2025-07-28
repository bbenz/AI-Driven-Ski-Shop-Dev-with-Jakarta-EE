package com.skishop.cart.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InventoryService {
    
    private static final Logger LOG = Logger.getLogger(InventoryService.class);
    
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Fallback(fallbackMethod = "checkStockFallback")
    public boolean checkStock(String sku, int requestedQuantity) {
        LOG.infof("Checking stock for SKU: %s, quantity: %d", sku, requestedQuantity);
        
        // TODO: Implement actual REST client call to inventory service
        // For now, we'll simulate stock check
        
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive");
        }
        
        // Mock stock availability (assume always available for demo)
        boolean available = true;
        
        LOG.debugf("Stock check result for SKU %s: %s", sku, available ? "Available" : "Not Available");
        
        return available;
    }
    
    public boolean checkStockFallback(String sku, int requestedQuantity) {
        LOG.warnf("Stock check fallback for SKU: %s - assuming available", sku);
        // In fallback, we assume stock is available to avoid blocking cart operations
        return true;
    }
    
    @Retry(maxRetries = 2)
    @Fallback(fallbackMethod = "reserveStockFallback")
    public boolean reserveStock(String sku, int quantity) {
        LOG.infof("Reserving stock for SKU: %s, quantity: %d", sku, quantity);
        
        // TODO: Implement actual REST client call to inventory service
        // For now, simulate reservation
        
        return true;
    }
    
    public boolean reserveStockFallback(String sku, int quantity) {
        LOG.warnf("Stock reservation fallback for SKU: %s - assuming reserved", sku);
        return true;
    }
    
    @Retry(maxRetries = 2)
    public void releaseStock(String sku, int quantity) {
        LOG.infof("Releasing stock for SKU: %s, quantity: %d", sku, quantity);
        
        // TODO: Implement actual REST client call to inventory service
        // For now, simulate release
    }
    
    public void releaseStockFallback(String sku, int quantity) {
        LOG.warnf("Stock release fallback for SKU: %s", sku);
    }
    
    @Retry(maxRetries = 2)
    @Fallback(fallbackMethod = "getAvailableStockFallback")
    public int getAvailableStock(String sku) {
        LOG.infof("Getting available stock for SKU: %s", sku);
        
        // TODO: Implement actual REST client call to inventory service
        // For now, return mock availability
        
        return 100; // Mock availability
    }
    
    public int getAvailableStockFallback(String sku) {
        LOG.warnf("Available stock fallback for SKU: %s - returning 0", sku);
        return 0;
    }
}

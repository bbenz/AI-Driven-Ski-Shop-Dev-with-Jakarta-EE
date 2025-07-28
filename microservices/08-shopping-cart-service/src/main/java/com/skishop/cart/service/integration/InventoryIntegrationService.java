package com.skishop.cart.service.integration;

import com.skishop.cart.client.InventoryManagementClient;
import com.skishop.cart.dto.CartItemDto;
import com.skishop.cart.dto.external.*;
import com.skishop.cart.service.CartCacheService;
import com.skishop.cart.service.CartService;
import com.skishop.cart.websocket.CartWebSocketService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Integration service for Inventory Management Service
 */
@ApplicationScoped
public class InventoryIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryIntegrationService.class);
    
    @Inject
    @RestClient
    InventoryManagementClient inventoryClient;
    
    @Inject
    CartService cartService;
    
    @Inject
    CartCacheService cartCacheService;
    
    @Inject
    CartWebSocketService cartWebSocketService;
    
    @Fallback(fallbackMethod = "reserveCartInventoryFallback")
    public InventoryReservationDto reserveCartInventory(String cartId, List<CartItemDto> items) {
        logger.debug("Reserving inventory for cart: {} with {} items", cartId, items.size());
        
        InventoryReservationRequestDto request = new InventoryReservationRequestDto(
            cartId,
            items.stream()
                .map(item -> new InventoryItemRequestDto(
                    item.productId(), 
                    item.quantity()
                ))
                .toList()
        );
        return inventoryClient.reserveInventory(request);
    }
    
    @Fallback(fallbackMethod = "getInventoryStatusFallback")
    public InventoryStatusDto getInventoryStatus(String productId) {
        logger.debug("Checking inventory status for product: {}", productId);
        return inventoryClient.getInventoryStatus(productId);
    }
    
    @Fallback(fallbackMethod = "checkBatchAvailabilityFallback")
    public List<InventoryAvailabilityDto> checkBatchAvailability(List<String> productIds) {
        logger.debug("Checking batch availability for {} products", productIds.size());
        return inventoryClient.checkBatchAvailability(productIds);
    }
    
    public void confirmReservation(String reservationId) {
        logger.debug("Confirming inventory reservation: {}", reservationId);
        inventoryClient.confirmReservation(reservationId);
    }
    
    public void releaseReservation(String reservationId) {
        logger.debug("Releasing inventory reservation: {}", reservationId);
        inventoryClient.releaseReservation(reservationId);
    }
    
    @Incoming("inventory-depleted")
    public void handleInventoryDepleted(String message) {
        logger.info("Handling inventory depleted event: {}", message);
        // Parse the event and mark items as out of stock
        // Implementation would depend on the event format
    }
    
    @Incoming("inventory-restored")
    public void handleInventoryRestored(String message) {
        logger.info("Handling inventory restored event: {}", message);
        // Parse the event and mark items as in stock
        // Implementation would depend on the event format
    }
    
    // Fallback methods
    public InventoryReservationDto reserveCartInventoryFallback(String cartId, List<CartItemDto> items) {
        logger.warn("Using fallback for inventory reservation. CartId: {}", cartId);
        return new InventoryReservationDto(
            "FALLBACK-" + cartId,
            cartId,
            "PENDING",
            null,
            BigDecimal.ZERO
        );
    }
    
    public InventoryStatusDto getInventoryStatusFallback(String productId) {
        logger.warn("Using fallback for inventory status. ProductId: {}", productId);
        return new InventoryStatusDto(
            productId,
            0,
            0,
            false,
            10,
            null
        );
    }
    
    public List<InventoryAvailabilityDto> checkBatchAvailabilityFallback(List<String> productIds) {
        logger.warn("Using fallback for batch availability check. ProductIds: {}", productIds);
        return productIds.stream()
            .map(productId -> new InventoryAvailabilityDto(productId, false, 0))
            .toList();
    }
}

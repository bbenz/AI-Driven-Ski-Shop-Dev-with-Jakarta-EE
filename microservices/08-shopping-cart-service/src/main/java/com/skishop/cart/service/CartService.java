package com.skishop.cart.service;

import com.skishop.cart.dto.*;
import com.skishop.cart.entity.ShoppingCart;
import com.skishop.cart.entity.CartItem;
import com.skishop.cart.exception.CartItemNotFoundException;
import com.skishop.cart.exception.CartNotFoundException;
import com.skishop.cart.exception.CartProcessingException;
import com.skishop.cart.exception.InsufficientStockException;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheInvalidate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class CartService {
    
    private static final Logger LOG = Logger.getLogger(CartService.class);
    
    @Inject
    CartCacheService cacheService;
    
    @Inject
    CartEventService eventService;
    
    @Inject
    ProductCatalogService productCatalogService;
    
    @Inject
    InventoryService inventoryService;
    
    @CacheResult(cacheName = "cart-summaries")
    @Transactional
    public CompletionStage<CartResponse> getCartByCartId(String cartId) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.infof("Getting cart by ID: %s", cartId);
            
            // Validate cartId format (should be UUID)
            if (cartId == null || cartId.trim().isEmpty()) {
                throw new CartNotFoundException("Cart ID cannot be null or empty");
            }
            
            // Try to parse cartId as UUID to validate format
            try {
                UUID.fromString(cartId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid cart ID format: " + cartId);
            }
            
            // Try to get from cache first
            CartResponse cachedCart = cacheService.getCachedCart(cartId);
            if (cachedCart != null) {
                LOG.debugf("Cart found in cache: %s", cartId);
                return cachedCart;
            }
            
            // Get from database
            Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
            if (cartOpt.isEmpty()) {
                throw new CartNotFoundException("Cart not found: " + cartId);
            }
            
            ShoppingCart cart = cartOpt.get();
            
            try {
                CartResponse response = CartResponse.from(cart);
                
                // Cache the result
                cacheService.cacheCart(cartId, response);
                
                return response;
            } catch (Exception e) {
                LOG.errorf(e, "Error creating CartResponse for cart: %s", cartId);
                throw new CartProcessingException("Error processing cart data", e);
            }
        });
    }
    
    @Transactional
    public CompletionStage<CartResponse> getOrCreateCart(UUID customerId, String sessionId) {
        LOG.infof("Getting or creating cart for customer: %s, session: %s", customerId, sessionId);
        
        ShoppingCart cart = null;
        
        // Try to find existing cart by customer ID
        if (customerId != null) {
            Optional<ShoppingCart> cartOpt = ShoppingCart.findByCustomerId(customerId);
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
            }
        }
        
        // If no customer cart found, try session cart
        if (cart == null && sessionId != null) {
            Optional<ShoppingCart> cartOpt = ShoppingCart.findBySessionId(sessionId);
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
            }
        }
        
        // Create new cart if none found
        if (cart == null) {
            cart = new ShoppingCart();
            cart.customerId = customerId;
            cart.sessionId = sessionId;
            cart.expiresAt = LocalDateTime.now().plusHours(8); // 8 hours expiry
            cart.persist();
            
            LOG.infof("Created new cart: %s", cart.cartId);
            
            // Publish cart created event
            // eventService.publishCartCreatedEvent(cart);
        }
        
        CartResponse response = CartResponse.from(cart);
        cacheService.cacheCart(cart.cartId, response);
        
        return CompletableFuture.completedFuture(response);
    }
    
    @Transactional
    @CacheInvalidate(cacheName = "cart-summaries")
    @Retry(maxRetries = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    public CompletionStage<CartResponse> addItem(String cartId, AddItemToCartRequest request) {
        LOG.infof("Adding item to cart %s: SKU=%s, Quantity=%d", cartId, request.sku(), request.quantity());
        
        // Validate cartId format
        validateCartId(cartId);
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        
        // Validate product exists and price
        productCatalogService.validateProduct(request.productId(), request.unitPrice());
        
        // Check inventory availability
        boolean stockAvailable = inventoryService.checkStock(request.sku(), request.quantity());
        if (!stockAvailable) {
            throw new InsufficientStockException("Insufficient stock for SKU: " + request.sku());
        }
        
        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = CartItem.findByCartAndSku(cart, request.sku());
        
        if (existingItemOpt.isPresent()) {
            // Update quantity of existing item
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.quantity + request.quantity();
            
            // Verify total quantity is available
            boolean totalStockAvailable = inventoryService.checkStock(request.sku(), newQuantity);
            if (!totalStockAvailable) {
                throw new InsufficientStockException("Insufficient stock for total quantity: " + newQuantity);
            }
            
            existingItem.updateQuantity(newQuantity);
            existingItem.persist();
            
            LOG.infof("Updated existing item quantity: %s -> %d", request.sku(), newQuantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.cart = cart;
            newItem.productId = request.productId();
            newItem.sku = request.sku();
            newItem.productName = request.productName();
            newItem.productImageUrl = request.productImageUrl();
            newItem.unitPrice = request.unitPrice();
            newItem.quantity = request.quantity();
            newItem.persist();
            
            cart.addItem(newItem);
            
            LOG.infof("Added new item to cart: %s", request.sku());
        }
        
        // Recalculate cart totals
        cart.calculateTotals();
        cart.persist();
        
        CartResponse response = CartResponse.from(cart);
        
        // Update cache
        cacheService.cacheCart(cartId, response);
        
        // Publish cart item added event
        // eventService.publishCartItemAddedEvent(cart, request.sku(), request.quantity());
        
        return CompletableFuture.completedFuture(response);
    }
    
    @Transactional
    @CacheInvalidate(cacheName = "cart-summaries")
    public CompletionStage<CartResponse> updateItemQuantity(String cartId, String sku, UpdateCartItemRequest request) {
        LOG.infof("Updating item quantity in cart %s: SKU=%s, Quantity=%d", cartId, sku, request.quantity());
        
        // Validate cartId format
        validateCartId(cartId);
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        
        Optional<CartItem> itemOpt = CartItem.findByCartAndSku(cart, sku);
        if (itemOpt.isEmpty()) {
            throw new CartItemNotFoundException("Item not found in cart: " + sku);
        }
        
        CartItem item = itemOpt.get();
        
        // Check inventory for new quantity
        boolean stockAvailable = inventoryService.checkStock(sku, request.quantity());
        if (!stockAvailable) {
            throw new InsufficientStockException("Insufficient stock for quantity: " + request.quantity());
        }
        
        int oldQuantity = item.quantity;
        item.updateQuantity(request.quantity());
        item.persist();
        
        // Recalculate cart totals
        cart.calculateTotals();
        cart.persist();
        
        CartResponse response = CartResponse.from(cart);
        
        // Update cache
        cacheService.cacheCart(cartId, response);
        
        // Publish cart item updated event
        // eventService.publishCartItemQuantityUpdatedEvent(cart, sku, oldQuantity, request.quantity());
        
        LOG.infof("Updated item quantity: %s %d -> %d", sku, oldQuantity, request.quantity());
        
        return CompletableFuture.completedFuture(response);
    }
    
    @Transactional
    @CacheInvalidate(cacheName = "cart-summaries")
    public CompletionStage<CartResponse> removeItem(String cartId, String sku) {
        LOG.infof("Removing item from cart %s: SKU=%s", cartId, sku);
        
        // Validate cartId format
        validateCartId(cartId);
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        
        Optional<CartItem> itemOpt = CartItem.findByCartAndSku(cart, sku);
        if (itemOpt.isEmpty()) {
            throw new CartItemNotFoundException("Item not found in cart: " + sku);
        }
        
        CartItem item = itemOpt.get();
        
        cart.removeItem(item);
        item.delete();
        
        // Recalculate cart totals
        cart.calculateTotals();
        cart.persist();
        
        CartResponse response = CartResponse.from(cart);
        
        // Update cache
        cacheService.cacheCart(cartId, response);
        
        // Publish cart item removed event
        // eventService.publishCartItemRemovedEvent(cart, sku);
        
        LOG.infof("Removed item from cart: %s", sku);
        
        return CompletableFuture.completedFuture(response);
    }
    
    @Transactional
    @CacheInvalidate(cacheName = "cart-summaries")
    public CompletionStage<Void> clearCart(String cartId) {
        LOG.infof("Clearing cart: %s", cartId);
        
        // Validate cartId format (should be UUID)
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new CartNotFoundException("Cart ID cannot be null or empty");
        }
        
        try {
            UUID.fromString(cartId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cart ID format: " + cartId);
        }
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        
        try {
            // Clear all cart items
            cart.items.clear();
            cart.persist();
            
            // Clear cache
            cacheService.invalidateCart(cartId);
            
            // Publish cart cleared event
            // eventService.publishCartClearedEvent(cart);
            
            LOG.infof("Cleared cart: %s", cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Error clearing cart items for cart: %s", cartId);
            throw new CartProcessingException("Error clearing cart", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Transactional
    public CompletionStage<CartResponse> mergeGuestCart(String guestCartId, UUID customerId) {
        LOG.infof("Merging guest cart %s with customer %s", guestCartId, customerId);
        
        Optional<ShoppingCart> guestCartOpt = ShoppingCart.findByCartId(guestCartId);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().isEmpty()) {
            // Return existing customer cart or create new one
            return getOrCreateCart(customerId, null);
        }
        
        ShoppingCart guestCart = guestCartOpt.get();
        
        Optional<ShoppingCart> customerCartOpt = ShoppingCart.findByCustomerId(customerId);
        if (customerCartOpt.isEmpty()) {
            // Convert guest cart to customer cart
            guestCart.customerId = customerId;
            guestCart.sessionId = null;
            guestCart.persist();
            
            CartResponse response = CartResponse.from(guestCart);
            cacheService.cacheCart(guestCart.cartId, response);
            
            // Publish cart merged event
            // eventService.publishCartMergedEvent(guestCart, guestCartId, guestCart.cartId);
            
            return CompletableFuture.completedFuture(response);
        }
        
        ShoppingCart customerCart = customerCartOpt.get();
        
        // Merge items from guest cart to customer cart
        for (CartItem guestItem : guestCart.items) {
            Optional<CartItem> existingItemOpt = CartItem.findByCartAndSku(customerCart, guestItem.sku);
            
            if (existingItemOpt.isPresent()) {
                // Update quantity
                CartItem existingItem = existingItemOpt.get();
                existingItem.updateQuantity(existingItem.quantity + guestItem.quantity);
                existingItem.persist();
            } else {
                // Add new item
                CartItem newItem = new CartItem();
                newItem.cart = customerCart;
                newItem.productId = guestItem.productId;
                newItem.sku = guestItem.sku;
                newItem.productName = guestItem.productName;
                newItem.productImageUrl = guestItem.productImageUrl;
                newItem.unitPrice = guestItem.unitPrice;
                newItem.quantity = guestItem.quantity;
                newItem.persist();
                
                customerCart.addItem(newItem);
            }
        }
        
        // Recalculate totals
        customerCart.calculateTotals();
        customerCart.persist();
        
        // Delete guest cart
        guestCart.delete();
        
        CartResponse response = CartResponse.from(customerCart);
        
        // Update cache
        cacheService.cacheCart(customerCart.cartId, response);
        cacheService.invalidateCart(guestCartId);
        
        // Publish cart merged event
        // eventService.publishCartMergedEvent(customerCart, guestCartId, customerCart.cartId);
        
        LOG.infof("Merged guest cart %s into customer cart %s", guestCartId, customerCart.cartId);
        
        return CompletableFuture.completedFuture(response);
    }
    
    @Timeout(5000)
    @Transactional
    public CompletionStage<CartValidationResponse> validateCart(String cartId) {
        LOG.infof("Validating cart: %s", cartId);
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        
        // Comprehensive cart validation
        boolean isValid = true;
        String errorMessage = null;
        
        try {
            // Product existence and pricing validation
            for (CartItem item : cart.items) {
                productCatalogService.validateProduct(item.productId, item.unitPrice);
                
                // Inventory availability validation
                boolean stockAvailable = inventoryService.checkStock(item.sku, item.quantity);
                if (!stockAvailable) {
                    isValid = false;
                    errorMessage = "Insufficient stock for SKU: " + item.sku;
                    break;
                }
            }
            
            // Coupon validity validation
            if (isValid && cart.appliedCoupons != null) {
                for (var coupon : cart.appliedCoupons) {
                    // Validate coupon is still active and applicable
                    // This would call the coupon service
                    LOG.debugf("Validating coupon: %s", coupon.couponCode);
                }
            }
            
            // Basic cart rules validation
            if (isValid && cart.items.isEmpty()) {
                isValid = false;
                errorMessage = "Cart is empty";
            }
            
        } catch (Exception e) {
            isValid = false;
            errorMessage = e.getMessage();
            LOG.errorf(e, "Cart validation failed for cart: %s", cartId);
        }
        
        if (isValid) {
            return CompletableFuture.completedFuture(new CartValidationResponse(true, java.util.List.of()));
        } else {
            return CompletableFuture.completedFuture(new CartValidationResponse(false, java.util.List.of(errorMessage)));
        }
    }
    
    private void validateCartId(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new CartNotFoundException("Cart ID cannot be null or empty");
        }
        
        // Try to parse cartId as UUID to validate format
        try {
            UUID.fromString(cartId);
        } catch (IllegalArgumentException e) {
            throw new CartNotFoundException("Invalid cart ID format: " + cartId);
        }
    }
}

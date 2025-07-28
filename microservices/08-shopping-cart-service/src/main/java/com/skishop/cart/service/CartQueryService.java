package com.skishop.cart.service;

import com.skishop.cart.dto.AppliedCouponDto;
import com.skishop.cart.dto.CartItemDto;
import com.skishop.cart.dto.CartSummaryDto;
import com.skishop.cart.entity.AppliedCoupon;
import com.skishop.cart.entity.CartItem;
import com.skishop.cart.entity.ShoppingCart;
import com.skishop.cart.exception.CartNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for querying cart information with optimized read operations
 */
@ApplicationScoped
public class CartQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartQueryService.class);
    
    @Inject
    EntityManager entityManager;
    
    @Inject
    CartCacheService cartCacheService;
    
    /**
     * Get cart summary with caching
     */
    @Transactional
    public CartSummaryDto getCartSummary(String cartId, String customerId) {
        logger.debug("Getting cart summary for cartId: {}, customerId: {}", cartId, customerId);
        
        // Try to get from cache first
        CartSummaryDto cachedSummary = cartCacheService.getCachedCartSummary(cartId, customerId);
        if (cachedSummary != null) {
            logger.debug("Returning cached cart summary for cartId: {}", cartId);
            return cachedSummary;
        }
        
        // Load from database
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartIdAndCustomerId(cartId, customerId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        ShoppingCart cart = cartOpt.get();
        CartSummaryDto summary = buildCartSummary(cart);
        
        // Cache the result
        cartCacheService.cacheCartSummary(cartId, customerId, summary);
        
        return summary;
    }
    
    /**
     * Get cart summary by cart ID only (for anonymous carts)
     */
    @Transactional
    public CartSummaryDto getCartSummary(String cartId) {
        logger.debug("Getting cart summary for cartId: {}", cartId);
        
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            throw new CartNotFoundException("Cart not found: " + cartId);
        }
        
        return buildCartSummary(cartOpt.get());
    }
    
    /**
     * Check if cart exists
     */
    @Transactional
    public boolean cartExists(String cartId, String customerId) {
        if (customerId != null) {
            return ShoppingCart.findByCartIdAndCustomerId(cartId, customerId).isPresent();
        } else {
            return ShoppingCart.findByCartId(cartId).isPresent();
        }
    }
    
    /**
     * Get all carts for a customer
     */
    @Transactional
    public List<CartSummaryDto> getCustomerCarts(String customerId) {
        logger.debug("Getting all carts for customerId: {}", customerId);
        
        List<ShoppingCart> carts = ShoppingCart.findByCustomerId(customerId);
        return carts.stream()
            .map(this::buildCartSummary)
            .toList();
    }
    
    /**
     * Get cart items count
     */
    @Transactional
    public int getCartItemsCount(String cartId) {
        Optional<ShoppingCart> cartOpt = ShoppingCart.findByCartId(cartId);
        if (cartOpt.isEmpty()) {
            return 0;
        }
        
        return cartOpt.get().items.size();
    }
    
    private CartSummaryDto buildCartSummary(ShoppingCart cart) {
        List<CartItemDto> items = cart.items.stream()
            .map(this::convertToCartItemDto)
            .toList();
        
        List<AppliedCouponDto> appliedCoupons = cart.appliedCoupons != null 
            ? cart.appliedCoupons.stream()
                .map(this::convertToAppliedCouponDto)
                .toList()
            : Collections.emptyList();
        
        return new CartSummaryDto(
            cart.cartId,
            cart.customerId != null ? cart.customerId.toString() : null,
            cart.sessionId,
            cart.status.toString(),
            items,
            items.size(),
            cart.subtotalAmount,
            cart.taxAmount,
            cart.shippingAmount,
            cart.discountAmount,
            cart.totalAmount,
            cart.currency,
            appliedCoupons,
            cart.createdAt,
            cart.updatedAt,
            cart.expiresAt
        );
    }
    
    private CartItemDto convertToCartItemDto(CartItem item) {
        return new CartItemDto(
            item.id.toString(),
            item.cart.cartId,
            item.productId != null ? item.productId.toString() : null,
            item.sku,
            item.productName,
            item.productImageUrl,
            item.unitPrice,
            item.quantity,
            item.totalPrice,
            Collections.emptyMap(), // Options would be stored separately if needed
            item.addedAt,
            item.updatedAt
        );
    }
    
    private AppliedCouponDto convertToAppliedCouponDto(AppliedCoupon coupon) {
        return new AppliedCouponDto(
            coupon.id.toString(),
            coupon.cart.cartId,
            coupon.couponCode,
            coupon.couponName,
            coupon.discountType.toString(),
            coupon.discountAmount,
            coupon.appliedAt
        );
    }
}

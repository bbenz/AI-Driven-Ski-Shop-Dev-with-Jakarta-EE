package com.skishop.cart.service.integration;

import com.skishop.cart.client.CouponServiceClient;
import com.skishop.cart.dto.external.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Integration service for Coupon Discount Service
 */
@ApplicationScoped
public class CouponIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CouponIntegrationService.class);
    
    @Inject
    @RestClient
    CouponServiceClient couponClient;
    
    @Fallback(fallbackMethod = "validateCouponFallback")
    public CouponValidationDto validateCoupon(String couponCode, String customerId, BigDecimal cartTotal) {
        logger.debug("Validating coupon: {} for customer: {}", couponCode, customerId);
        return couponClient.validateCoupon(couponCode, customerId, cartTotal.toString());
    }
    
    @Fallback(fallbackMethod = "applyCouponFallback")
    public CouponApplicationResultDto applyCoupon(String cartId, String couponCode, String customerId, BigDecimal subtotal) {
        logger.debug("Applying coupon: {} to cart: {}", couponCode, cartId);
        
        CouponApplicationDto applicationDto = new CouponApplicationDto(
            couponCode, cartId, customerId, subtotal
        );
        
        return couponClient.applyCoupon(applicationDto);
    }
    
    public void removeCoupon(String cartId, String couponCode) {
        logger.debug("Removing coupon: {} from cart: {}", couponCode, cartId);
        couponClient.removeCoupon(cartId, couponCode);
    }
    
    // Fallback methods
    public CouponValidationDto validateCouponFallback(String couponCode, String customerId, BigDecimal cartTotal) {
        logger.warn("Using fallback for coupon validation. Coupon: {}", couponCode);
        return new CouponValidationDto(
            couponCode,
            false,
            "PERCENTAGE",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "Coupon service unavailable",
            "Coupon validation service is temporarily unavailable"
        );
    }
    
    public CouponApplicationResultDto applyCouponFallback(String cartId, String couponCode, String customerId, BigDecimal subtotal) {
        logger.warn("Using fallback for coupon application. Coupon: {}", couponCode);
        return new CouponApplicationResultDto(
            false,
            BigDecimal.ZERO,
            subtotal,
            couponCode,
            "Coupon service is temporarily unavailable"
        );
    }
}

package com.skishop.cart.service.integration;

import com.skishop.cart.client.LoyaltyServiceClient;
import com.skishop.cart.dto.external.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Integration service for Loyalty Points Service
 */
@ApplicationScoped
public class LoyaltyIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyIntegrationService.class);
    
    @Inject
    @RestClient
    LoyaltyServiceClient loyaltyClient;
    
    @Fallback(fallbackMethod = "getCustomerPointsFallback")
    public LoyaltyPointsDto getCustomerPoints(String customerId) {
        logger.debug("Getting loyalty points for customer: {}", customerId);
        return loyaltyClient.getCustomerPoints(customerId);
    }
    
    @Fallback(fallbackMethod = "calculateEarnedPointsFallback")
    public PointsCalculationDto calculateEarnedPoints(String customerId, BigDecimal cartTotal) {
        logger.debug("Calculating earned points for customer: {} with cart total: {}", customerId, cartTotal);
        return loyaltyClient.calculateEarnedPoints(customerId, cartTotal.toString());
    }
    
    @Fallback(fallbackMethod = "redeemPointsFallback")
    public PointsRedemptionDto redeemPoints(String customerId, BigDecimal pointsToRedeem) {
        logger.debug("Redeeming points for customer: {}, points: {}", customerId, pointsToRedeem);
        return loyaltyClient.redeemPoints(customerId, pointsToRedeem.toString());
    }
    
    // Fallback methods
    public LoyaltyPointsDto getCustomerPointsFallback(String customerId) {
        logger.warn("Using fallback for customer points. Customer: {}", customerId);
        return new LoyaltyPointsDto(
            customerId,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "STANDARD",
            BigDecimal.ZERO
        );
    }
    
    public PointsCalculationDto calculateEarnedPointsFallback(String customerId, BigDecimal cartTotal) {
        logger.warn("Using fallback for points calculation. Customer: {}", customerId);
        return new PointsCalculationDto(
            cartTotal,
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.ZERO
        );
    }
    
    public PointsRedemptionDto redeemPointsFallback(String customerId, BigDecimal pointsToRedeem) {
        logger.warn("Using fallback for points redemption. Customer: {}", customerId);
        return new PointsRedemptionDto(
            customerId,
            pointsToRedeem,
            BigDecimal.ZERO,
            false,
            null
        );
    }
}

package com.skishop.cart.client;

import com.skishop.cart.dto.external.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * REST client for Loyalty Points Service integration
 */
@Path("/api/v1/loyalty")
@RegisterRestClient(configKey = "loyalty-points-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface LoyaltyServiceClient {

    @GET
    @Path("/points/{customerId}")
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    LoyaltyPointsDto getCustomerPoints(@PathParam("customerId") String customerId);

    @POST
    @Path("/calculate")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    PointsCalculationDto calculateEarnedPoints(@QueryParam("customerId") String customerId,
                                              @QueryParam("cartTotal") String cartTotal);

    @POST
    @Path("/redeem")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    PointsRedemptionDto redeemPoints(@QueryParam("customerId") String customerId,
                                    @QueryParam("points") String pointsToRedeem);

    @GET
    @Path("/health")
    @Timeout(5000)
    void healthCheck();
}

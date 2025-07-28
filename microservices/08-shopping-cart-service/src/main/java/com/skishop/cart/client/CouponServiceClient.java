package com.skishop.cart.client;

import com.skishop.cart.dto.external.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * REST client for Coupon Discount Service integration
 */
@Path("/api/v1/coupons")
@RegisterRestClient(configKey = "coupon-discount-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CouponServiceClient {

    @POST
    @Path("/validate")
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    CouponValidationDto validateCoupon(@QueryParam("code") String couponCode, 
                                      @QueryParam("customerId") String customerId,
                                      @QueryParam("cartTotal") String cartTotal);

    @POST
    @Path("/apply")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    CouponApplicationResultDto applyCoupon(CouponApplicationDto applicationDto);

    @DELETE
    @Path("/remove")
    @Retry(maxRetries = 2, delay = 1000)
    @Timeout(10000)
    void removeCoupon(@QueryParam("cartId") String cartId, 
                     @QueryParam("couponCode") String couponCode);

    @GET
    @Path("/health")
    @Timeout(5000)
    void healthCheck();
}

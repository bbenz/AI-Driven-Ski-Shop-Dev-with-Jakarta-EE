package com.skishop.cart.client;

import com.skishop.cart.dto.external.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.util.List;

/**
 * REST client for Inventory Management Service integration
 */
@Path("/api/v1/inventory")
@RegisterRestClient(configKey = "inventory-management-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface InventoryManagementClient {

    @GET
    @Path("/{productId}")
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    InventoryStatusDto getInventoryStatus(@PathParam("productId") String productId);

    @POST
    @Path("/reserve")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    InventoryReservationDto reserveInventory(InventoryReservationRequestDto request);

    @POST
    @Path("/confirm-reservation")
    @Retry(maxRetries = 3, delay = 1000)
    @Timeout(10000)
    void confirmReservation(@QueryParam("reservationId") String reservationId);

    @POST
    @Path("/release-reservation")
    @Retry(maxRetries = 3, delay = 1000)
    @Timeout(10000)
    void releaseReservation(@QueryParam("reservationId") String reservationId);

    @POST
    @Path("/check-availability")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    List<InventoryAvailabilityDto> checkBatchAvailability(List<String> productIds);

    @GET
    @Path("/health")
    @Timeout(5000)
    void healthCheck();
}

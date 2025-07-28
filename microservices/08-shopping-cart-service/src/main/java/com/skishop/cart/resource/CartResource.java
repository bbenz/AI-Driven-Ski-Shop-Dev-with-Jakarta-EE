package com.skishop.cart.resource;

import com.skishop.cart.dto.*;
import com.skishop.cart.exception.*;
import com.skishop.cart.service.CartService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.UUID;

@Path("/api/v1/carts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shopping Cart", description = "Shopping cart management operations")
public class CartResource {
    
    private static final Logger LOG = Logger.getLogger(CartResource.class);
    
    @Inject
    CartService cartService;
    
    @GET
    @Path("/{cartId}")
    @Blocking
    @Operation(summary = "Get cart by ID", description = "Retrieve cart information by cart ID")
    @APIResponse(responseCode = "200", description = "Cart retrieved successfully", 
                content = @Content(schema = @Schema(implementation = CartResponse.class)))
    @APIResponse(responseCode = "404", description = "Cart not found")
    @APIResponse(responseCode = "400", description = "Invalid cart ID")
    public Uni<Response> getCart(@PathParam("cartId") String cartId) {
        LOG.infof("GET /api/v1/carts/%s", cartId);
        
        return Uni.createFrom().completionStage(cartService.getCartByCartId(cartId))
            .map(cart -> Response.ok(cart).build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(IllegalArgumentException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_CART_ID", ex.getMessage()))
                    .build())
            .onFailure(CartProcessingException.class)
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error processing cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("CART_PROCESSING_ERROR", "Error processing cart data"))
                    .build();
            })
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Unexpected error getting cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @GET
    @Path("/session/{sessionId}")
    @Blocking
    @Operation(summary = "Get or create cart by session", description = "Get or create cart for a session")
    @APIResponse(responseCode = "200", description = "Cart retrieved or created successfully")
    public Uni<Response> getOrCreateCartBySession(@PathParam("sessionId") String sessionId) {
        LOG.infof("GET /api/v1/carts/session/%s", sessionId);
        
        return Uni.createFrom().completionStage(cartService.getOrCreateCart(null, sessionId))
            .map(cart -> Response.ok(cart).build())
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error getting/creating cart for session: %s", sessionId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @GET
    @Path("/customer/{customerId}")
    @Blocking
    @Operation(summary = "Get or create cart by customer", description = "Get or create cart for a customer")
    @APIResponse(responseCode = "200", description = "Cart retrieved or created successfully")
    public Uni<Response> getOrCreateCartByCustomer(@PathParam("customerId") String customerId) {
        LOG.infof("GET /api/v1/carts/customer/%s", customerId);
        
        try {
            UUID customerUuid = UUID.fromString(customerId);
            
            return Uni.createFrom().completionStage(cartService.getOrCreateCart(customerUuid, null))
                .map(cart -> Response.ok(cart).build())
                .onFailure()
                .recoverWithItem(ex -> {
                    LOG.errorf(ex, "Error getting/creating cart for customer: %s", customerId);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                        .build();
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_CUSTOMER_ID", "Invalid customer ID format"))
                    .build()
            );
        }
    }
    
    @POST
    @Path("/{cartId}/items")
    @Blocking
    @Operation(summary = "Add item to cart", description = "Add a product item to the cart")
    @APIResponse(responseCode = "200", description = "Item added successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Cart not found")
    @APIResponse(responseCode = "409", description = "Insufficient stock")
    public Uni<Response> addItem(
            @PathParam("cartId") String cartId,
            @Valid AddItemToCartRequest request) {
        
        LOG.infof("POST /api/v1/carts/%s/items - SKU: %s, Quantity: %d", 
            cartId, request.sku(), request.quantity());
        
        return Uni.createFrom().completionStage(cartService.addItem(cartId, request))
            .map(cart -> Response.ok(cart).build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(InsufficientStockException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage()))
                    .build())
            .onFailure(IllegalArgumentException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_REQUEST", ex.getMessage()))
                    .build())
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error adding item to cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @PUT
    @Path("/{cartId}/items/{sku}/quantity")
    @Blocking
    @Operation(summary = "Update item quantity", description = "Update the quantity of an item in the cart")
    @APIResponse(responseCode = "200", description = "Quantity updated successfully")
    @APIResponse(responseCode = "404", description = "Cart or item not found")
    @APIResponse(responseCode = "409", description = "Insufficient stock")
    public Uni<Response> updateItemQuantity(
            @PathParam("cartId") String cartId,
            @PathParam("sku") String sku,
            @Valid UpdateCartItemRequest request) {
        
        LOG.infof("PUT /api/v1/carts/%s/items/%s/quantity - Quantity: %d", 
            cartId, sku, request.quantity());
        
        return Uni.createFrom().completionStage(cartService.updateItemQuantity(cartId, sku, request))
            .map(cart -> Response.ok(cart).build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(CartItemNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("ITEM_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(InsufficientStockException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage()))
                    .build())
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error updating item quantity in cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @DELETE
    @Path("/{cartId}/items/{sku}")
    @Blocking
    @Operation(summary = "Remove item from cart", description = "Remove an item from the cart")
    @APIResponse(responseCode = "200", description = "Item removed successfully")
    @APIResponse(responseCode = "404", description = "Cart or item not found")
    public Uni<Response> removeItem(
            @PathParam("cartId") String cartId,
            @PathParam("sku") String sku) {
        
        LOG.infof("DELETE /api/v1/carts/%s/items/%s", cartId, sku);
        
        return Uni.createFrom().completionStage(cartService.removeItem(cartId, sku))
            .map(cart -> Response.ok(cart).build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(CartItemNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("ITEM_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error removing item from cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @DELETE
    @Path("/{cartId}/items")
    @Blocking
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    @APIResponse(responseCode = "204", description = "Cart cleared successfully")
    @APIResponse(responseCode = "404", description = "Cart not found")
    @APIResponse(responseCode = "400", description = "Invalid cart ID")
    public Uni<Response> clearCart(@PathParam("cartId") String cartId) {
        LOG.infof("DELETE /api/v1/carts/%s/items", cartId);
        
        return Uni.createFrom().completionStage(cartService.clearCart(cartId))
            .map(v -> Response.noContent().build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure(IllegalArgumentException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_CART_ID", ex.getMessage()))
                    .build())
            .onFailure(CartProcessingException.class)
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error clearing cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("CART_PROCESSING_ERROR", "Error clearing cart"))
                    .build();
            })
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Unexpected error clearing cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
    
    @POST
    @Path("/{guestCartId}/merge/{customerId}")
    @Blocking
    @Operation(summary = "Merge guest cart", description = "Merge guest cart with customer cart")
    @APIResponse(responseCode = "200", description = "Cart merged successfully")
    @APIResponse(responseCode = "404", description = "Cart not found")
    public Uni<Response> mergeGuestCart(
            @PathParam("guestCartId") String guestCartId,
            @PathParam("customerId") String customerId) {
        
        LOG.infof("POST /api/v1/carts/%s/merge/%s", guestCartId, customerId);
        
        try {
            UUID customerUuid = UUID.fromString(customerId);
            
            return Uni.createFrom().completionStage(cartService.mergeGuestCart(guestCartId, customerUuid))
                .map(cart -> Response.ok(cart).build())
                .onFailure(CartNotFoundException.class)
                .recoverWithItem(ex -> 
                    Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                        .build())
                .onFailure()
                .recoverWithItem(ex -> {
                    LOG.errorf(ex, "Error merging guest cart: %s", guestCartId);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                        .build();
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_CUSTOMER_ID", "Invalid customer ID format"))
                    .build()
            );
        }
    }
    
    @POST
    @Path("/{cartId}/validate")
    @Blocking
    @Operation(summary = "Validate cart", description = "Validate cart contents and integrity")
    @APIResponse(responseCode = "200", description = "Validation result")
    public Uni<Response> validateCart(@PathParam("cartId") String cartId) {
        LOG.infof("POST /api/v1/carts/%s/validate", cartId);
        
        return Uni.createFrom().completionStage(cartService.validateCart(cartId))
            .map(validationResult -> Response.ok(validationResult).build())
            .onFailure(CartNotFoundException.class)
            .recoverWithItem(ex -> 
                Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("CART_NOT_FOUND", ex.getMessage()))
                    .build())
            .onFailure()
            .recoverWithItem(ex -> {
                LOG.errorf(ex, "Error validating cart: %s", cartId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"))
                    .build();
            });
    }
}

package com.skishop.cart.service;

import com.skishop.cart.event.*;
import com.skishop.cart.entity.ShoppingCart;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class CartEventService {
    
    private static final Logger LOG = Logger.getLogger(CartEventService.class);
    
    @Inject
    @Channel("cart-events")
    Emitter<JsonObject> cartEventEmitter;
    
    public void publishCartCreatedEvent(ShoppingCart cart) {
        try {
            JsonObject eventJson = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CART_CREATED")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId != null ? cart.customerId.toString() : null)
                .put("sessionId", cart.sessionId)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(eventJson);
            LOG.infof("Published CartCreatedEvent for cart: %s", cart.cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartCreatedEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartItemAddedEvent(ShoppingCart cart, String sku, int quantity) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartItemAdded")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("sku", sku)
                .put("quantity", quantity)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartItemAddedEvent for cart: %s, SKU: %s", cart.cartId, sku);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartItemAddedEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartItemRemovedEvent(ShoppingCart cart, String sku) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartItemRemoved")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("sku", sku)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartItemRemovedEvent for cart: %s, SKU: %s", cart.cartId, sku);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartItemRemovedEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartItemQuantityUpdatedEvent(ShoppingCart cart, String sku, 
                                                   Integer oldQuantity, Integer newQuantity) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartItemQuantityUpdated")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("sku", sku)
                .put("oldQuantity", oldQuantity)
                .put("newQuantity", newQuantity)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartItemQuantityUpdatedEvent for cart: %s, SKU: %s", cart.cartId, sku);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartItemQuantityUpdatedEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartClearedEvent(ShoppingCart cart) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartCleared")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartClearedEvent for cart: %s", cart.cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartClearedEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartMergedEvent(ShoppingCart guestCart, String guestCartId, String userCartId) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartMerged")
                .put("guestCartId", guestCartId)
                .put("userCartId", userCartId)
                .put("customerId", guestCart.customerId)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartMergedEvent for guest cart: %s, user cart: %s", guestCartId, userCartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartMergedEvent for guest cart: %s", guestCartId);
        }
    }
    
    public void publishCartCheckedOutEvent(ShoppingCart cart, String orderId) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartCheckedOut")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("orderId", orderId)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartCheckedOutEvent for cart: %s, order: %s", cart.cartId, orderId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartCheckedOutEvent for cart: %s", cart.cartId);
        }
    }
    
    public void publishCartAbandonedEvent(ShoppingCart cart) {
        try {
            JsonObject event = new JsonObject()
                .put("eventId", UUID.randomUUID().toString())
                .put("eventType", "CartAbandoned")
                .put("cartId", cart.cartId)
                .put("customerId", cart.customerId)
                .put("timestamp", LocalDateTime.now().toString());
            
            cartEventEmitter.send(event);
            LOG.infof("Published CartAbandonedEvent for cart: %s", cart.cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish CartAbandonedEvent for cart: %s", cart.cartId);
        }
    }
}

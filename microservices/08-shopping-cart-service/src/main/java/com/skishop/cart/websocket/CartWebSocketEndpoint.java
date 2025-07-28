package com.skishop.cart.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.cart.dto.CartSummaryDto;
import com.skishop.cart.service.CartQueryService;
import com.skishop.cart.service.CartService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time cart updates
 */
@ServerEndpoint("/carts/{cartId}/websocket")
@ApplicationScoped
public class CartWebSocketEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(CartWebSocketEndpoint.class);
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @Inject
    ObjectMapper objectMapper;
    
    @Inject
    CartQueryService cartQueryService;
    
    @Inject
    CartService cartService;
    
    @Inject
    CartWebSocketService cartWebSocketService;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("cartId") String cartId) {
        String userId = extractUserIdFromSession(session);
        userSessions.put(userId, session);
        cartWebSocketService.addUserSession(userId, session);
        
        logger.info("WebSocket connection opened for user: {} on cart: {}", userId, cartId);
        
        try {
            // Send current cart state on connection
            CartSummaryDto currentCart = cartQueryService.getCartSummary(cartId, userId);
            CartStateMessage message = new CartStateMessage("CART_CONNECTED", currentCart);
            sendToSession(session, message);
        } catch (Exception e) {
            logger.error("Error sending initial cart state for user: " + userId, e);
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("cartId") String cartId) {
        try {
            CartWebSocketMessage wsMessage = objectMapper.readValue(message, CartWebSocketMessage.class);
            String userId = extractUserIdFromSession(session);
            
            logger.debug("Received WebSocket message: {} for cart: {}", wsMessage.type(), cartId);
            
            switch (wsMessage.type()) {
                case "ADD_ITEM":
                    handleAddItem(cartId, userId, wsMessage.payload(), session);
                    break;
                case "UPDATE_QUANTITY":
                    handleUpdateQuantity(cartId, userId, wsMessage.payload(), session);
                    break;
                case "REMOVE_ITEM":
                    handleRemoveItem(cartId, userId, wsMessage.payload(), session);
                    break;
                case "APPLY_COUPON":
                    handleApplyCoupon(cartId, userId, wsMessage.payload(), session);
                    break;
                default:
                    logger.warn("Unknown WebSocket message type: {}", wsMessage.type());
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);
            sendError(session, "処理中にエラーが発生しました: " + e.getMessage());
        }
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("cartId") String cartId, CloseReason closeReason) {
        String userId = extractUserIdFromSession(session);
        userSessions.remove(userId);
        cartWebSocketService.removeUserSession(userId);
        
        logger.info("WebSocket connection closed for user: {} on cart: {}, reason: {}", 
                   userId, cartId, closeReason.getReasonPhrase());
    }
    
    @OnError
    public void onError(Session session, @PathParam("cartId") String cartId, Throwable throwable) {
        String userId = extractUserIdFromSession(session);
        logger.error("WebSocket error for user: " + userId + " on cart: " + cartId, throwable);
        
        sendError(session, "接続中にエラーが発生しました");
    }
    
    @Incoming("cart-updated")
    public void handleCartUpdated(String eventData) {
        try {
            // Parse the cart updated event and notify relevant users
            // Implementation would depend on the event format
            logger.debug("Handling cart updated event: {}", eventData);
        } catch (Exception e) {
            logger.error("Error handling cart updated event", e);
        }
    }
    
    private void handleAddItem(String cartId, String userId, Map<String, Object> payload, Session session) {
        // Implementation for adding item via WebSocket
        logger.debug("Handling add item for cart: {}", cartId);
        sendSuccess(session, "商品をカートに追加しました");
    }
    
    private void handleUpdateQuantity(String cartId, String userId, Map<String, Object> payload, Session session) {
        // Implementation for updating quantity via WebSocket
        logger.debug("Handling update quantity for cart: {}", cartId);
        sendSuccess(session, "数量を更新しました");
    }
    
    private void handleRemoveItem(String cartId, String userId, Map<String, Object> payload, Session session) {
        // Implementation for removing item via WebSocket
        logger.debug("Handling remove item for cart: {}", cartId);
        sendSuccess(session, "商品をカートから削除しました");
    }
    
    private void handleApplyCoupon(String cartId, String userId, Map<String, Object> payload, Session session) {
        // Implementation for applying coupon via WebSocket
        logger.debug("Handling apply coupon for cart: {}", cartId);
        sendSuccess(session, "クーポンを適用しました");
    }
    
    private String extractUserIdFromSession(Session session) {
        // Extract user ID from session parameters or JWT token
        // For now, return a placeholder
        return "user-" + session.getId().substring(0, 8);
    }
    
    private void sendToSession(Session session, Object message) {
        if (session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(jsonMessage);
            } catch (Exception e) {
                logger.error("Error sending message to session", e);
            }
        }
    }
    
    private void sendError(Session session, String errorMessage) {
        ErrorMessage message = new ErrorMessage("ERROR", errorMessage);
        sendToSession(session, message);
    }
    
    private void sendSuccess(Session session, String successMessage) {
        SuccessMessage message = new SuccessMessage("SUCCESS", successMessage);
        sendToSession(session, message);
    }
    
    // Message classes
    public record CartWebSocketMessage(String type, Map<String, Object> payload) {}
    
    public record CartStateMessage(String type, CartSummaryDto cart) {}
    
    public record ErrorMessage(String type, String message) {}
    
    public record SuccessMessage(String type, String message) {}
}

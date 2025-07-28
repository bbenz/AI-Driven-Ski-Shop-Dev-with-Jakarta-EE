package com.skishop.cart.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skishop.cart.dto.CartSummaryDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for real-time cart updates
 */
@ApplicationScoped
public class CartWebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartWebSocketService.class);
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @Inject
    ObjectMapper objectMapper;
    
    public void addUserSession(String userId, Session session) {
        userSessions.put(userId, session);
        logger.info("WebSocket session added for user: {}", userId);
    }
    
    public void removeUserSession(String userId) {
        userSessions.remove(userId);
        logger.info("WebSocket session removed for user: {}", userId);
    }
    
    public void notifyCartUpdate(String userId, CartSummaryDto cartSummary) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                WebSocketMessage message = new WebSocketMessage("CART_UPDATED", cartSummary);
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(jsonMessage);
                logger.debug("Cart update notification sent to user: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to send cart update notification to user: " + userId, e);
            }
        }
    }
    
    public void notifyInventoryUpdate(String productId, boolean inStock) {
        InventoryUpdateMessage message = new InventoryUpdateMessage("INVENTORY_UPDATE", productId, inStock);
        
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(message);
                    session.getAsyncRemote().sendText(jsonMessage);
                } catch (Exception e) {
                    logger.error("Failed to send inventory update notification", e);
                }
            }
        });
        
        logger.debug("Inventory update notification broadcasted for product: {}, inStock: {}", productId, inStock);
    }
    
    public void broadcastToAll(Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            userSessions.values().forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(jsonMessage);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to broadcast message to all users", e);
        }
    }
    
    // Message classes
    public record WebSocketMessage(String type, Object payload) {}
    
    public record InventoryUpdateMessage(String type, String productId, boolean inStock) {}
}

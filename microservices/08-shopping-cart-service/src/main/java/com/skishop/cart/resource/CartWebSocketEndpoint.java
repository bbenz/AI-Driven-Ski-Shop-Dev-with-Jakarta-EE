package com.skishop.cart.resource;

import com.skishop.cart.dto.WebSocketMessage;
import com.skishop.cart.service.CartService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/v1/carts/ws/{cartId}")
@ApplicationScoped
public class CartWebSocketEndpoint {
    
    private static final Logger LOG = Logger.getLogger(CartWebSocketEndpoint.class);
    
    @Inject
    CartService cartService;
    
    @Inject
    ObjectMapper objectMapper;
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("cartId") String cartId) {
        sessions.put(cartId, session);
        LOG.infof("WebSocket connection opened for cart: %s", cartId);
        
        // Send connection confirmation
        sendMessage(session, new WebSocketMessage("connected", 
            Map.of("cartId", cartId, "timestamp", LocalDateTime.now())));
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("cartId") String cartId) {
        sessions.remove(cartId);
        LOG.infof("WebSocket connection closed for cart: %s", cartId);
    }
    
    @OnError
    public void onError(Session session, @PathParam("cartId") String cartId, Throwable throwable) {
        LOG.errorf(throwable, "WebSocket error for cart: %s", cartId);
        sessions.remove(cartId);
    }
    
    @OnMessage
    public void onMessage(String message, @PathParam("cartId") String cartId) {
        LOG.debugf("WebSocket message received for cart %s: %s", cartId, message);
        
        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message, WebSocketMessage.class);
            handleWebSocketMessage(cartId, wsMessage);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process WebSocket message for cart: %s", cartId);
            
            Session session = sessions.get(cartId);
            if (session != null) {
                sendMessage(session, new WebSocketMessage("error", 
                    Map.of("message", "Invalid message format")));
            }
        }
    }
    
    public void broadcastCartUpdate(String cartId, Object cartData) {
        Session session = sessions.get(cartId);
        if (session != null && session.isOpen()) {
            WebSocketMessage message = new WebSocketMessage("cart_updated", 
                Map.of("cart", cartData, "timestamp", LocalDateTime.now()));
            sendMessage(session, message);
        }
    }
    
    public void broadcastPriceUpdate(String cartId, String sku, Object oldPrice, Object newPrice) {
        Session session = sessions.get(cartId);
        if (session != null && session.isOpen()) {
            WebSocketMessage message = new WebSocketMessage("price_updated", 
                Map.of(
                    "sku", sku,
                    "oldPrice", oldPrice,
                    "newPrice", newPrice,
                    "timestamp", LocalDateTime.now()
                ));
            sendMessage(session, message);
        }
    }
    
    public void broadcastStockUpdate(String cartId, String sku, boolean available) {
        Session session = sessions.get(cartId);
        if (session != null && session.isOpen()) {
            WebSocketMessage message = new WebSocketMessage("stock_updated", 
                Map.of(
                    "sku", sku,
                    "available", available,
                    "timestamp", LocalDateTime.now()
                ));
            sendMessage(session, message);
        }
    }
    
    public void notifyProductDiscontinued(String cartId, String sku) {
        Session session = sessions.get(cartId);
        if (session != null && session.isOpen()) {
            WebSocketMessage message = new WebSocketMessage("product_discontinued", 
                Map.of(
                    "sku", sku,
                    "message", "This product has been discontinued",
                    "timestamp", LocalDateTime.now()
                ));
            sendMessage(session, message);
        }
    }
    
    private void handleWebSocketMessage(String cartId, WebSocketMessage message) {
        Session session = sessions.get(cartId);
        if (session == null) {
            return;
        }
        
        switch (message.type()) {
            case "ping" -> {
                sendMessage(session, new WebSocketMessage("pong", 
                    Map.of("timestamp", LocalDateTime.now())));
            }
            case "get_cart" -> {
                // Asynchronously fetch and send cart data
                cartService.getCartByCartId(cartId)
                    .whenComplete((cart, throwable) -> {
                        if (throwable != null) {
                            sendMessage(session, new WebSocketMessage("error", 
                                Map.of("message", "Failed to retrieve cart data")));
                        } else {
                            sendMessage(session, new WebSocketMessage("cart_data", 
                                Map.of("cart", cart)));
                        }
                    });
            }
            case "subscribe_updates" -> {
                sendMessage(session, new WebSocketMessage("subscribed", 
                    Map.of("cartId", cartId, "timestamp", LocalDateTime.now())));
            }
            default -> {
                LOG.warnf("Unknown WebSocket message type: %s", message.type());
                sendMessage(session, new WebSocketMessage("error", 
                    Map.of("message", "Unknown message type: " + message.type())));
            }
        }
    }
    
    private void sendMessage(Session session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String messageJson = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(messageJson);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send WebSocket message");
        }
    }
    
    // Utility methods for external use
    public boolean hasActiveConnection(String cartId) {
        Session session = sessions.get(cartId);
        return session != null && session.isOpen();
    }
    
    public int getActiveConnectionCount() {
        return (int) sessions.values().stream()
            .filter(Session::isOpen)
            .count();
    }
    
    public void closeConnection(String cartId) {
        Session session = sessions.get(cartId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
                sessions.remove(cartId);
                LOG.infof("Closed WebSocket connection for cart: %s", cartId);
            } catch (Exception e) {
                LOG.errorf(e, "Error closing WebSocket connection for cart: %s", cartId);
            }
        }
    }
}

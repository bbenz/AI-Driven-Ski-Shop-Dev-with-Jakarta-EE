package com.skishop.cart.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartCreatedEvent(
    String eventId,
    String cartId,
    UUID customerId,
    String sessionId,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_CREATED";
    }
}

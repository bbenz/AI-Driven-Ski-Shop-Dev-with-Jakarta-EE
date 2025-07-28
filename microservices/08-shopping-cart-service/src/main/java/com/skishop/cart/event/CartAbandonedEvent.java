package com.skishop.cart.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartAbandonedEvent(
    String eventId,
    String cartId,
    UUID customerId,
    String sessionId,
    int itemCount,
    BigDecimal totalAmount,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_ABANDONED";
    }
}

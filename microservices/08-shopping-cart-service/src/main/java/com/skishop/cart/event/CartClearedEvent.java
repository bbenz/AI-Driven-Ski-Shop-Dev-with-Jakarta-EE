package com.skishop.cart.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartClearedEvent(
    String eventId,
    String cartId,
    UUID customerId,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_CLEARED";
    }
}

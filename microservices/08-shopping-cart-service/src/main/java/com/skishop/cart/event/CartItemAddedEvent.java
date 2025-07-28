package com.skishop.cart.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemAddedEvent(
    String eventId,
    String cartId,
    UUID customerId,
    String sku,
    int quantity,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_ITEM_ADDED";
    }
}

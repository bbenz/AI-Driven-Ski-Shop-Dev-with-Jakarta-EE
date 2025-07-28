package com.skishop.cart.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemRemovedEvent(
    String eventId,
    String cartId,
    UUID customerId,
    String sku,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_ITEM_REMOVED";
    }
}
